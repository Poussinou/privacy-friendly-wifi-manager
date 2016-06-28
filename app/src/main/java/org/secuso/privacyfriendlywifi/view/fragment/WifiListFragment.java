package org.secuso.privacyfriendlywifi.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.secuso.privacyfriendlywifi.logic.types.WifiLocationEntry;
import org.secuso.privacyfriendlywifi.logic.util.IOnDialogClosedListener;
import org.secuso.privacyfriendlywifi.logic.util.ScreenHandler;
import org.secuso.privacyfriendlywifi.logic.util.WifiListHandler;
import org.secuso.privacyfriendlywifi.view.adapter.WifiListAdapter;
import org.secuso.privacyfriendlywifi.view.decoration.DividerItemDecoration;
import org.secuso.privacyfriendlywifi.view.dialog.WifiPickerDialog;

import java.util.Observable;
import java.util.Observer;

import secuso.org.privacyfriendlywifi.R;

public class WifiListFragment extends Fragment implements IOnDialogClosedListener, Observer {
    private IOnDialogClosedListener thisClass;
    private WifiListHandler wifiListHandler;

    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private Context context;

    public WifiListFragment() {
        // Required empty public constructor
        this.thisClass = this;
    }

    public static WifiListFragment newInstance() {
        WifiListFragment fragment = new WifiListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_wifilist, container, false);

        this.context = getContext();
        this.wifiListHandler = new WifiListHandler(getContext());
        this.wifiListHandler.addObserver(this);

        // Set substring in actionbar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setSubtitle(R.string.fragment_wifilist);
        }

        // setup the floating action button
        this.fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        if (this.fab != null) {
            this.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final WifiManager wifiMan = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
                    if (!wifiMan.isWifiEnabled()) {
                        Snackbar.make(view, R.string.wifi_enable_wifi_to_scan, Snackbar.LENGTH_LONG)
                                .setAction(R.string.wifi_enable_wifi, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                wifiMan.setWifiEnabled(true);
                                                fab.callOnClick();
                                            }
                                        }
                                ).show();
                    } else {
                        WifiPickerDialog dialog = new WifiPickerDialog(getContext());
                        dialog.addOnDialogClosedListener(thisClass);
                        dialog.setManagedWifis(wifiListHandler.getAll());
                        dialog.show();
                    }
                }
            });
        }

        // setup recycler view
        this.recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getBaseContext()));

        WifiListAdapter itemsAdapter = new WifiListAdapter(getActivity().getBaseContext(), R.layout.list_item_wifilist, this.wifiListHandler, this.recyclerView, fab);

        // save list after item deletion
        itemsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });

        this.recyclerView.setAdapter(itemsAdapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));

        if (this.fab != null) {
            this.recyclerView.setPadding(
                    this.recyclerView.getPaddingLeft(),
                    this.recyclerView.getPaddingTop(),
                    this.recyclerView.getPaddingRight(),
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                            ScreenHandler.getPXFromDP(this.fab.getPaddingTop() + this.fab.getHeight() + fab.getPaddingBottom(), this.getContext())
                            : this.fab.getPaddingTop() + this.fab.getHeight() + this.fab.getPaddingBottom()));
        }

        return rootView;
    }

    @Override
    public void onDialogClosed(int returnCode, Object... returnValue) {
        if (returnCode == DialogInterface.BUTTON_POSITIVE) {
            this.wifiListHandler.add((WifiLocationEntry) returnValue[0]);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void update(Observable observable, Object data) {
        this.recyclerView.swapAdapter(new WifiListAdapter(this.context, R.layout.list_item_wifilist, this.wifiListHandler, this.recyclerView, this.fab), false);
        this.recyclerView.requestLayout();
        this.recyclerView.invalidate();
    }
}