package com.m1nist3r.taskventure.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.m1nist3r.taskventure.R;
import com.m1nist3r.taskventure.activities.auth.SignUpActivity;
import com.m1nist3r.taskventure.adapter.TaskGroupBuyableAdapter;
import com.m1nist3r.taskventure.databinding.ActivityPaymentBinding;
import com.m1nist3r.taskventure.gateway.GPaymentGateway;
import com.m1nist3r.taskventure.model.task.ITaskGroupBuyableService;
import com.m1nist3r.taskventure.model.task.ITaskGroupService;
import com.m1nist3r.taskventure.model.task.TaskGroup;
import com.m1nist3r.taskventure.model.task.TaskGroupBuyable;
import com.m1nist3r.taskventure.model.task.TaskGroupBuyableServiceFirebaseImpl;
import com.m1nist3r.taskventure.model.task.TaskGroupServiceFirebaseImpl;
import com.m1nist3r.taskventure.util.CheckNetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

import static com.m1nist3r.taskventure.util.GlobalHelper.displayMobileDataSettingsDialog;
import static com.m1nist3r.taskventure.util.GlobalVariables.isNetworkConnected;

public class PaymentActivity extends AppCompatActivity {

    // Arbitrarily-picked constant integer you define to track a request for payment data activity.
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    private static final long SHIPPING_COST_CENTS = 90 * GPaymentGateway.CENTS_IN_A_UNIT.longValue();

    // A client for interacting with the Google Pay API.
    private PaymentsClient paymentsClient;

    private ActivityPaymentBinding layoutBinding;
    private View googlePayButton;

    private FirebaseAuth mAuth;
    private ITaskGroupBuyableService taskGroupBuyableService;
    private ITaskGroupService taskGroupService;
    private TaskGroupBuyableAdapter mAdapter;
    private TaskGroupBuyable taskGroupBuyable;

    /**
     * Initialize the Google Pay API on creation of the activity
     *
     * @see Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUi();

        // Initialize a Google Pay API client for an environment suitable for testing.
        // It's recommended to create the PaymentsClient object inside of the onCreate method.
        paymentsClient = GPaymentGateway.createPaymentsClient(this);
        possiblyShowGooglePayButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();

        if (isNetworkConnected && mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {
                startActivity(new Intent(this, SignUpActivity.class));
            }
        } else if (!isNetworkConnected) {
            AlertDialog dialog = displayMobileDataSettingsDialog(this);
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode  Result code returned by the Google Pay API.
     * @param data        Intent from the Google Pay API containing payment or error data.
     * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
     * from an Activity</a>
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // value passed in AutoResolveHelper
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(data);
                    assert paymentData != null;
                    handlePaymentSuccess(paymentData);
                    saveRecentlyPurchasedTaskGroup();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user cancelled the payment attempt
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    assert status != null;
                    handleError(status.getStatusCode());
                    break;
            }

            // Re-enables the Google Pay payment button.
            googlePayButton.setClickable(true);
        }
    }

    private void saveRecentlyPurchasedTaskGroup() {
        TaskGroupBuyable taskGroupBuyable = mAdapter.getSelectedTaskGroup();
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setId(UUID.randomUUID().toString());
        taskGroup.setName(taskGroupBuyable.getName());
        taskGroup.setImagePath(taskGroupBuyable.getImagePath());
        taskGroup.setTaskList(taskGroupBuyable.getTaskList());
        taskGroupService.saveTask(taskGroup);
    }

    private void initializeUi() {
        CheckNetwork checkNetwork = new CheckNetwork(getApplicationContext());
        checkNetwork.registerNetworkCallback();

        mAuth = FirebaseAuth.getInstance();
        taskGroupBuyableService = new TaskGroupBuyableServiceFirebaseImpl();
        taskGroupService = new TaskGroupServiceFirebaseImpl();

        // Use view binding to access the UI elements
        layoutBinding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());

        RecyclerView recyclerView = layoutBinding.recyclerViewShop;
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        FirestoreRecyclerOptions<TaskGroupBuyable> recyclerOptions =
                new FirestoreRecyclerOptions.Builder<TaskGroupBuyable>()
                        .setQuery(taskGroupBuyableService.findAllTaskGroupBuyable(), TaskGroupBuyable.class)
                        .build();

        mAdapter = new TaskGroupBuyableAdapter(recyclerOptions,
                getApplicationContext());
        recyclerView.setAdapter(mAdapter);

        googlePayButton = layoutBinding.googlePayButton.getRoot();
        googlePayButton.setOnClickListener(this::requestPayment);
    }

    private void possiblyShowGooglePayButton() {

        final Optional<JSONObject> isReadyToPayJson = GPaymentGateway.getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(this,
                task1 -> {
                    if (task1.isSuccessful()) {
                        setGooglePayAvailable(task1.getResult());
                    } else {
                        Log.w("isReadyToPay failed", task1.getException());
                    }
                });
    }

    /**
     * If isReadyToPay returned {@code true}, show the button and hide the "checking" text. Otherwise,
     * notify the user that Google Pay is not available. Please adjust to fit in with your current
     * user flow. You are not required to explicitly let the user know if isReadyToPay returns {@code
     * false}.
     *
     * @param available isReadyToPay API response.
     */
    private void setGooglePayAvailable(boolean available) {
        if (available) {
            googlePayButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.googlepay_status_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see <a href="https://developers.google.com/pay/api/android/reference/
     * object#PaymentData">PaymentData</a>
     */
    private void handlePaymentSuccess(PaymentData paymentData) {

        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        final String paymentInfo = paymentData.toJson();
        if (paymentInfo == null) {
            return;
        }

        try {
            JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".

            final JSONObject tokenizationData = paymentMethodData.getJSONObject("tokenizationData");
            final String token = tokenizationData.getString("token");
            final JSONObject info = paymentMethodData.getJSONObject("info");
            final String billingName = info.getJSONObject("billingAddress").getString("name");
            Toast.makeText(
                    this, getString(R.string.payments_show_name, billingName),
                    Toast.LENGTH_LONG).show();

            // Logging token string.
            Log.d("Google Pay token: ", token);

        } catch (JSONException e) {
            throw new RuntimeException("The selected garment cannot be parsed from the list of elements");
        }
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     *                   WalletConstants.ERROR_CODE_* constants.
     * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/
     * WalletConstants#constant-summary">Wallet Constants Library</a>
     */
    private void handleError(int statusCode) {
        Log.e("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    public void requestPayment(View view) {
        if (mAdapter.getSelectedTaskGroup() != null) {
            // Disables the button to prevent multiple clicks.
            googlePayButton.setClickable(false);

            // The price provided to the API should include taxes and shipping.
            // This price is not displayed to the user.
            taskGroupBuyable = mAdapter.getSelectedTaskGroup();

            double garmentPrice = Double.parseDouble(taskGroupBuyable.getPrice());
            long garmentPriceCents = Math.round(garmentPrice * GPaymentGateway.CENTS_IN_A_UNIT.longValue());
            long priceCents = garmentPriceCents + SHIPPING_COST_CENTS;

            Optional<JSONObject> paymentDataRequestJson = GPaymentGateway.getPaymentDataRequest(priceCents);
            if (!paymentDataRequestJson.isPresent()) {
                return;
            }

            PaymentDataRequest request =
                    PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());

            // Since loadPaymentData may show the UI asking the user to select a payment method, we use
            // AutoResolveHelper to wait for the user interacting with it. Once completed,
            // onActivityResult will be called with the result.
            if (request != null) {
                AutoResolveHelper.resolveTask(
                        paymentsClient.loadPaymentData(request),
                        this, LOAD_PAYMENT_DATA_REQUEST_CODE);
            }

        }
    }
}