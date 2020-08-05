package com.officinetop.officine.utils

import android.widget.Filter
import org.json.JSONArray
import org.json.JSONObject

class ListFilter(originalArray:JSONArray, filterKey: String): Filter() {

    var mOriginalArray:JSONArray
    var mFilterKey: String

    init {
        mOriginalArray = originalArray
        mFilterKey = filterKey
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var filterResults = FilterResults()

        if(constraint!= null && constraint.isNotEmpty())
        {
            var filteredList = ArrayList<JSONObject>()

//            constraint = constraint.toString().toLowerCase()

            for(i in 0 until mOriginalArray.length())
            {
                val json = JSONObject(mOriginalArray[i].toString())
                if(json.getString(mFilterKey) == constraint)
                {
                    filteredList.add(json)
                }
            }

            filterResults.count = filteredList.size
            filterResults.values = filteredList

//            for (int i=0;i<filterList.size();i++)
//            {
//                //CHECK
//                if(filterList.get(i).getName().toUpperCase().contains(constraint))
//                {
//                    //ADD PLAYER TO FILTERED PLAYERS
//                    filteredPlayers.add(filterList.get(i));
//                }
//            }
//
//            results.count=filteredPlayers.size();
//            results.values=filteredPlayers;
        }

        return filterResults
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /*MyAdapter adapter;
    ArrayList<Player> filterList;

    public CustomFilter(ArrayList<Player> filterList,MyAdapter adapter)
    {
        this.adapter=adapter;
        this.filterList=filterList;

    }

    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();

        //CHECK CONSTRAINT VALIDITY
        if(constraint != null && constraint.length() > 0)
        {
            //CHANGE TO UPPER
            constraint=constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<Player> filteredPlayers=new ArrayList<>();

            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                if(filterList.get(i).getName().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredPlayers.add(filterList.get(i));
                }
            }

            results.count=filteredPlayers.size();
            results.values=filteredPlayers;
        }else
        {
            results.count=filterList.size();
            results.values=filterList;

        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

       adapter.players= (ArrayList<Player>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();
    }*/
}