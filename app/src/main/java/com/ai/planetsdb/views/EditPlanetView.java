package com.ai.planetsdb.views;


import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ai.planetsdb.R;
import com.ai.planetsdb.model.PlanetModel;
import com.ai.planetsdb.util.Utils;

import java.util.ArrayList;

public class EditPlanetView {

    /**
     * The interface to send events from the view to the controller
     */
    public static interface ViewListener {
        public void onOkClick();
        public void onCancelClick();
        public void onEditorsValidate();
    }

    @SuppressWarnings("UnusedDeclaration")
    private static boolean DEBUG = false;
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = EditPlanetView.class.getSimpleName();

    private ArrayList<TextView> mEditViewList = new ArrayList<>();

    private final View mRootView;
    private final ScrollView mScrollView;
    private final TextView mLoadingMessage;
    private final EditText mPlanetName, mDistance, mDiscoverer, mDiameter;
    private final CheckBox mAtmosphere;
    private final Button mOkButton, mCancelButton;

    private final Activity mActivity;
    private PlanetModel mModel;
    private int mModification = Utils.MODIFY_UNINITIALIZED;

    private boolean mAutoCompletion;

    /**
     * The listener reference for sending events
     */
    private ViewListener viewListener;
    public void setViewListener(ViewListener viewListener) {
        this.viewListener = viewListener;
    }

    public EditPlanetView(Activity activity, View rootView) {
        mActivity = activity;

        mRootView = rootView;
        mScrollView = (ScrollView) mRootView.findViewById(R.id.scroll_view);
        mLoadingMessage = (TextView) mRootView.findViewById(R.id.loading_message);
        mPlanetName = (EditText) mRootView.findViewById(R.id.details_editText_planetName);
        mDistance = (EditText) mRootView.findViewById(R.id.details_editText_distance);
        mDiscoverer = (EditText) mRootView.findViewById(R.id.details_editText_discoverer);
        mDiameter = (EditText) mRootView.findViewById(R.id.details_editText_diameter);
        mAtmosphere = (CheckBox) mRootView.findViewById(R.id.details_checkBox_atmosphere);
        mOkButton = (Button) mRootView.findViewById(R.id.details_button_ok);
        mCancelButton = (Button) mRootView.findViewById(R.id.details_button_cancel);

        mEditViewList.add(mPlanetName);
        mEditViewList.add(mDistance);
        mEditViewList.add(mDiscoverer);
        mEditViewList.add(mDiameter);
        mEditViewList.add(mAtmosphere);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewListener.onOkClick();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewListener.onCancelClick();
            }
        });

        for (TextView textView : mEditViewList) {
            textView.addTextChangedListener(new TextValidator(textView) {
                @Override
                public void validate(TextView textView, String text) {
                    if (mAutoCompletion) return;
                    fillModelFromUI();
                    viewListener.onEditorsValidate();
                }
            });

        }

        // Display loading screen
        setModel(null);
    }

    /**
     * Fill in the view with the contents of the given event model. This allows
     * an edit view to be initialized before the event has been loaded. Passing
     * in null for the model will display a loading screen. A non-null model
     * will fill in the view's fields with the data contained in the model.
     *
     * @param model The event model to pull the data from
     */
    public void setModel(PlanetModel model) {
        mModel = model;

        if (model == null) {
            // Display loading screen
            mLoadingMessage.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
            return;
        }
        // TODO: is model fields check for null needed?
        mModel.consume(model);
        fillEditors(model);

        updateView();
        mScrollView.setVisibility(View.VISIBLE);
        mLoadingMessage.setVisibility(View.GONE);
    }

    private void fillEditors(PlanetModel model) {
        String str;
        mAutoCompletion = true;

        if ((str = model.mName) != null) { mPlanetName.setText(str); }
        else { mPlanetName.setText(""); }
        mDiameter.setText(String.format("%.0f", model.mDiameter));
        mDistance.setText(String.format("%.0f", model.mDistance));
        mAtmosphere.setChecked(model.mHasAtmosphere);
        if ((str = model.mDiscoverer) != null) { mDiscoverer.setText(str); }
        else { mDiscoverer.setText(""); }

        mAutoCompletion = false;
    }

    public void setOkButtonLabel(String label) {
        mOkButton.setText(label);
    }

    /**
     * Updates the view based on {@link #mModification} and {@link #mModel}
     */
    public void updateView() {
        if (mModel == null) {
            return;
        }
        setViewStates(mModification);
    }

    public void setViewStates(int mode) {
        boolean isInit = mode != Utils.MODIFY_UNINITIALIZED;
        for (View view : mEditViewList) {
            view.setEnabled(isInit);
            view.setClickable(isInit);
        }
        mOkButton.setEnabled(isInit);
    }

    public void setEditable(boolean isEditable) {
        for (View view : mEditViewList) {
            view.setEnabled(isEditable);
            view.setClickable(isEditable);
            view.setFocusable(isEditable);
        }
    }

    public void setModification(int modifyWhich) {
        mModification = modifyWhich;
        updateView();
    }

    public void setValidity(int errors) {
        if (errors == 0) {
            clearErrors();
            if (mModification != Utils.MODIFY_UNINITIALIZED) {
                mOkButton.setEnabled(true);
            }
            return;
        }

        if ((errors & Utils.ERROR_EMPTY_NAME) != 0) {
            mOkButton.setEnabled(false);
            mPlanetName.setError(mActivity.getResources().getString(
                    R.string.empty_name_error));
        }
    }

    private void clearErrors() {
        for (TextView view : mEditViewList) {
            view.setError(null);
        }
    }

    /**
     * Does prep steps for saving a planet.
     *
     * Checks if the planet is ready to be saved.
     *
     * @return false if there is no model, true otherwise.
     */
    public boolean prepareForSave() {
        return fillModelFromUI();
    }

    // Goes through the UI elements and updates the model as necessary
    private boolean fillModelFromUI() {
        if (mModel == null) {
            return false;
        }

        mModel.mName = mPlanetName.getText().toString();
        if (mModel.mName == null || mModel.mName.trim().length() == 0) {
            mModel.mName = null;
        }

        double distance;
        String text = mDistance.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            try {
                distance = Double.parseDouble(text);
            } catch (NumberFormatException ex) {
                distance = 0.0;
            }
            mModel.mDistance = distance;
        } else {
            mModel.mDistance = 0.0;
        }

        mModel.mDiscoverer = mDiscoverer.getText().toString();
        if (TextUtils.isEmpty(mModel.mDiscoverer)) {
            mModel.mDiscoverer = null;
        }

        double diameter;
        text = mDiameter.getText().toString();
        if (!text.isEmpty()) {
            try {
                diameter = Double.parseDouble(mDiameter.getText().toString());
            } catch (NumberFormatException ex) {
                diameter = 0.0;
            }
            mModel.mDiameter = diameter;
        } else {
            mModel.mDiameter = 0.0;
        }

        mModel.mHasAtmosphere = mAtmosphere.isChecked();

        return true;
    }

    private abstract class TextValidator implements TextWatcher {
        private final TextView textView;

        public TextValidator(TextView textView) {
            this.textView = textView;
        }

        public abstract void validate(TextView textView, String text);

        @Override
        final public void afterTextChanged(Editable s) {
            String text = textView.getText().toString();
            validate(textView, text);
        }

        @Override
        final public void beforeTextChanged(CharSequence s, int start,
                                            int count, int after) {
            /* Don't care */
        }

        @Override
        final public void onTextChanged(CharSequence s, int start,
                                        int before, int count) {
            /* Don't care */
        }
    }
}
