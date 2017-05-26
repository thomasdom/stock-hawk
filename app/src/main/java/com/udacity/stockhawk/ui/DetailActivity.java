package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final int PAGE_LIMIT = 2;
    public static final int STOCK_LOADER_ID = 1;

    // @formatter:off
    @State public boolean isDataLoaded = false;

    @BindView(R.id.toolbar) public Toolbar toolbar;
    @BindView(R.id.stock_name) public TextView tvStockName;
    @BindView(R.id.stock_price) public TextView tvStockPrice;
    @BindView(R.id.absolute_change) public TextView tvAbsoluteChange;
    @BindView(R.id.viewpager) public ViewPager viewPager;
    @BindView(R.id.tabs) public TabLayout tabLayout;
    // @formatter:on

    private Uri stockUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(getString(R.string.stock_details_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        stockUri = getIntent().getData();
        setupViewPager();
        tabLayout.setupWithViewPager(viewPager, true);
        getSupportLoaderManager().initLoader(STOCK_LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        if (null != stockUri)
        {
            return new CursorLoader(
                    this,
                    stockUri,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[Contract.Quote.QUOTE_COLUMNS.size()]),
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        if (data.moveToFirst())
        {
            String stockName = data.getString(Contract.Quote.POSITION_SYMBOL);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float absolutionChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            Float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            getWindow().getDecorView().setContentDescription(
                    String.format(getString(R.string.detail_activity_cd), stockName));

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
            dollarFormat.setMaximumFractionDigits(2);
            dollarFormat.setMinimumFractionDigits(2);

            tvStockName.setText(stockName);
            tvStockPrice.setText(dollarFormat.format(stockPrice));
            tvStockPrice.setContentDescription(String.format(getString(R.string.stock_price_cd), tvStockPrice.getText()));
            tvAbsoluteChange.setText(dollarFormat.format(absolutionChange));

            if (absolutionChange > 0)
            {
                tvAbsoluteChange.setBackgroundResource(R.drawable.percent_change_pill_green);
                tvAbsoluteChange.setContentDescription(
                        String.format(getString(R.string.stock_increment_cd), tvAbsoluteChange.getText()));
            }
            else
            {
                tvAbsoluteChange.setBackgroundResource(R.drawable.percent_change_pill_red);
                tvAbsoluteChange.setContentDescription(
                        String.format(getString(R.string.stock_decrement_cd), tvAbsoluteChange.getText()));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        isDataLoaded = false;
    }

    private void setupViewPager()
    {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        DetailFragment monthlyStock = new DetailFragment();
        bundle.putString(getString(R.string.FRAGMENT_DATA_TYPE_KEY), getString(R.string.MONTHLY));
        monthlyStock.setArguments(bundle);

        DetailFragment weeklyStock = new DetailFragment();
        bundle = new Bundle();
        bundle.putString(getString(R.string.FRAGMENT_DATA_TYPE_KEY), getString(R.string.WEEKLY));
        weeklyStock.setArguments(bundle);

        DetailFragment dailyStock = new DetailFragment();
        bundle = new Bundle();
        bundle.putString(getString(R.string.FRAGMENT_DATA_TYPE_KEY), getString(R.string.DAILY));
        dailyStock.setArguments(bundle);

        viewPagerAdapter.addFragment(dailyStock, getString(R.string.days_fragment_title));
        viewPagerAdapter.addFragment(weeklyStock, getString(R.string.weeks_fragment_title));
        viewPagerAdapter.addFragment(monthlyStock, getString(R.string.months_fragment_title));

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(PAGE_LIMIT);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter
    {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            return fragmentList.get(position);
        }

        @Override
        public int getCount()
        {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return fragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title)
        {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }
}
