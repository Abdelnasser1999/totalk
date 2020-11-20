package com.callberry.callingapp.countrypicker;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.callberry.callingapp.R;

import java.util.ArrayList;
import java.util.Collections;

public class CountryPickerFragment extends DialogFragment implements CountrySelectListener, TextWatcher {

    private RecyclerView listCountries;
    private ProgressBar progressBar;
    private CountryPickerAdapter adapter;
    private CountrySelectListener listener;
    private EditText editTextSearchCountry;

    public CountryPickerFragment(CountrySelectListener listener) {
        this.listener = listener;
    }

    public CountryPickerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_country_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        setAdapter();
    }

    private void setAdapter() {
        new Thread(() -> {
            ArrayList<Country> countries = getCountries();
            getActivity().runOnUiThread(() -> {
                adapter = new CountryPickerAdapter(countries, this);
                listCountries.setLayoutManager(new LinearLayoutManager(getContext()));
                listCountries.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            });
        }).start();

    }

    private ArrayList<Country> getCountries() {
        ArrayList<Country> countries;
        countries = JSONParser.countries(getActivity());
        Collections.sort(countries, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        return countries;
    }

    public void setListener(CountrySelectListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSelected(Country country) {
        if (listener != null) {
            listener.onSelected(country);
        }
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
    }

    private void initViews(View view) {
        listCountries = view.findViewById(R.id.list_countries);
        progressBar = view.findViewById(R.id.progressBar);
        editTextSearchCountry = view.findViewById(R.id.edit_txt_search_country);
        editTextSearchCountry.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        adapter.getFilter().filter(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        editTextSearchCountry.getText().clear();

    }
}
