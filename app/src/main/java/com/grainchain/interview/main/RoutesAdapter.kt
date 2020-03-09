package com.grainchain.interview.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.grainchain.interview.R
import com.grainchain.interview.data.Route

class RoutesAdapter(private var routes: List<Route> = emptyList(), private val listener: RouteClickListener) :
    Adapter<RoutesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val listItem = inflater.inflate(R.layout.item_list_routes, parent, false)
        return RoutesViewHolder(listItem)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    override fun onBindViewHolder(holder: RoutesViewHolder, position: Int) {
        val route = routes[position]
        holder.routeName.text = route.name

        holder.setClickListener { listener.onRouteClicked(route) }
    }

    fun updateRoutes(routes: List<Route>) {
        this.routes = routes
        notifyDataSetChanged()
    }
}

class RoutesViewHolder(private val v: View) : ViewHolder(v) {
    val routeName: TextView = v.findViewById(R.id.route_title)

    fun setClickListener(listener: () -> Unit) {
        v.setOnClickListener { listener() }
    }
}

interface RouteClickListener {
    fun onRouteClicked(route: Route)
}