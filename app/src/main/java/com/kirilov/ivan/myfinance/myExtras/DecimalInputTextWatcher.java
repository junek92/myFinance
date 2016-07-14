package com.kirilov.ivan.myfinance.myExtras;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ivan on 14-Jul-16.
 */
public class DecimalInputTextWatcher implements TextWatcher {

    private String mPreviousValue;
    private int mCursorPosition;
    private boolean mRestoringPreviousValueFlag;
    private int mDigitsAfterZero;
    private EditText mEditText;

    public DecimalInputTextWatcher(EditText editText, int digitsAfterZero) {
        mDigitsAfterZero = digitsAfterZero;
        mEditText = editText;
        mPreviousValue = "";
        mRestoringPreviousValueFlag = false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!mRestoringPreviousValueFlag) {
            mPreviousValue = s.toString();
            mCursorPosition = mEditText.getSelectionStart();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mRestoringPreviousValueFlag) {

            if (!isValid(s.toString())) {
                mRestoringPreviousValueFlag = true;
                restorePreviousValue();
            }

        } else {
            mRestoringPreviousValueFlag = false;
        }
    }

    private void restorePreviousValue() {
        mEditText.setText(mPreviousValue);
        mEditText.setSelection(mCursorPosition);
    }

    private boolean isValid(String s) {
        Pattern patternWithDot = Pattern.compile("[0-9]*((\\.[0-9]{0," + mDigitsAfterZero + "})?)||(\\.)?");
        Pattern patternWithComma = Pattern.compile("[0-9]*((,[0-9]{0," + mDigitsAfterZero + "})?)||(,)?");

        Matcher matcherDot = patternWithDot.matcher(s);
        Matcher matcherComa = patternWithComma.matcher(s);

        return matcherDot.matches() || matcherComa.matches();
    }
}