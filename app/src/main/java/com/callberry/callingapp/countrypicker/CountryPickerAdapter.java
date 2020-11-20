package com.callberry.callingapp.countrypicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.callberry.callingapp.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CountryPickerAdapter extends RecyclerView.Adapter<CountryPickerAdapter.Holder> implements Filterable {

    private ArrayList<Country> countries;
    private ArrayList<Country> countriesFullList = new ArrayList<>();
    private CountrySelectListener listener;

    public CountryPickerAdapter(ArrayList<Country> countries, CountrySelectListener listener) {
        this.countries = countries;
        this.listener = listener;
        countriesFullList.addAll(countries);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_country, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Country county = countries.get(position);
        holder.tvCountryName.setText(county.getName());
        holder.tvCountryCode.setText(county.getDialCode());
        holder.tvFlag.setText(county.getFlag());
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    @Override
    public Filter getFilter() {
        return _countryFilter;
    }

    private Filter _countryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Country> filteredCountries = new ArrayList<>();
            if (charSequence == null || charSequence.length() == 0) {
                filteredCountries.addAll(countriesFullList);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Country callRate : countriesFullList) {
                    if (callRate.getName().toLowerCase().contains(filterPattern)) {
                        filteredCountries.add(callRate);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredCountries;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            countries.clear();
            countries.addAll((Collection<? extends Country>) filterResults.values);
            notifyDataSetChanged();
        }
    };


    class Holder extends RecyclerView.ViewHolder {
        TextView tvFlag;
        TextView tvCountryName;
        TextView tvCountryCode;
        ConstraintLayout itemCountry;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvFlag = itemView.findViewById(R.id.tv_flag);
            tvCountryName = itemView.findViewById(R.id.tv_country_name);
            tvCountryCode = itemView.findViewById(R.id.tv_country_code);
            itemCountry = itemView.findViewById(R.id.item_country);
            itemCountry.setOnClickListener(view -> listener.onSelected(countries.get(getAdapterPosition())));
        }
    }
}
