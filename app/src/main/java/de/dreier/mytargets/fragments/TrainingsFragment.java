/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.recyclerviewchoicemode.SelectableViewHolder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.dreier.mytargets.R;
import de.dreier.mytargets.activities.SimpleFragmentActivity;
import de.dreier.mytargets.adapters.ExpandableNowListAdapter;
import de.dreier.mytargets.managers.dao.RoundDataSource;
import de.dreier.mytargets.managers.dao.TrainingDataSource;
import de.dreier.mytargets.models.Month;
import de.dreier.mytargets.shared.models.Round;
import de.dreier.mytargets.shared.models.Training;
import de.dreier.mytargets.utils.DataLoader;

/**
 * Shows an overview over all trying days
 */
public class TrainingsFragment extends ExpandableNowListFragment<Month, Training> {

    private TrainingDataSource trainingDataSource;

    public TrainingsFragment() {
        itemTypeRes = R.plurals.training_selected;
        itemTypeDelRes = R.plurals.training_deleted;
        newStringRes = R.string.new_training;
    }

    @Override
    public void onSelected(Training item) {
        Intent i = new Intent(getActivity(), SimpleFragmentActivity.TrainingActivity.class);
        i.putExtra(TRAINING_ID, item.getId());
        startActivity(i);
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onEdit(final Training item) {
        Intent i = new Intent(getActivity(), SimpleFragmentActivity.EditTrainingActivity.class);
        i.putExtra(EditTrainingFragment.TRAINING_ID, item.getId());
        startActivity(i);
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    public Loader<List<Training>> onCreateLoader(int id, Bundle args) {
        trainingDataSource = new TrainingDataSource(getContext());
        return new DataLoader<>(getContext(), trainingDataSource, trainingDataSource::getAll);
    }

    @Override
    public void onLoadFinished(Loader<List<Training>> loader, List<Training> data) {
        Set<Long> monthMap = new HashSet<>();
        List<Month> months = new ArrayList<>();
        for (Training t : data) {
            long parentId = t.getParentId();
            if (!monthMap.contains(parentId)) {
                monthMap.add(parentId);
                months.add(new Month(parentId));
            }
        }
        Collections.sort(months);
        setList(trainingDataSource, months, data, false, new TrainingAdapter());
        if (mAdapter.getItemCount() > 0) {
            mAdapter.expandOrCollapse(0);
        }
    }

    private class TrainingAdapter extends ExpandableNowListAdapter<Month, Training> {

        @Override
        protected HeaderViewHolder getTopLevelViewHolder(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_month, parent, false);
            return new HeaderViewHolder(itemView);
        }

        @Override
        protected ViewHolder getSecondLevelViewHolder(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_training, parent, false);
            return new ViewHolder(itemView);
        }
    }

    private class ViewHolder extends SelectableViewHolder<Training> {
        public final TextView mTitle;
        public final TextView mSubtitle;
        public final TextView mGes;

        public ViewHolder(View itemView) {
            super(itemView, mSelector, TrainingsFragment.this);
            mTitle = (TextView) itemView.findViewById(R.id.training);
            mSubtitle = (TextView) itemView.findViewById(R.id.training_date);
            mGes = (TextView) itemView.findViewById(R.id.gesTraining);
        }

        @Override
        public void bindCursor() {
            mTitle.setText(mItem.title);
            mSubtitle.setText(DateFormat.getDateInstance().format(mItem.date));
            int maxPoints = 0;
            int reachedPoints = 0;
            RoundDataSource roundDataSource = new RoundDataSource(getContext());
            ArrayList<Round> rounds = roundDataSource.getAll(mItem.getId());
            for(Round r:rounds) {
                maxPoints += r.info.getMaxPoints();
                reachedPoints += r.reachedPoints;
            }
            mGes.setText(reachedPoints + "/" + maxPoints);
        }
    }

    private class HeaderViewHolder extends SelectableViewHolder<Month> {
        public final TextView mTitle;

        public HeaderViewHolder(View itemView) {
            super(itemView, R.id.expand_collapse);
            mTitle = (TextView) itemView.findViewById(android.R.id.text1);
        }

        @Override
        public void bindCursor() {
            mTitle.setText(mItem.toString());
        }
    }
}