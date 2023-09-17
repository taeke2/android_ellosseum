//package com.gbsoft.ellosseum;
//
//import android.app.PendingIntent;
//import android.appwidget.AppWidgetManager;
//import android.appwidget.AppWidgetProvider;
//import android.content.Context;
//import android.content.Intent;
//import android.widget.RemoteViews;
//import android.widget.Toast;
//
//public class WidgetProvider extends AppWidgetProvider {
//
//    private PendingIntent getOn(Context context) {
//        Intent clickIntent = new Intent(context, WidgetProvider.class);
//        clickIntent.setAction(context.getResources().getString(R.string.getOn_kor));
//        return PendingIntent.getBroadcast(context, 0, clickIntent, 0);
//    }
//
//    private PendingIntent getOff(Context context) {
//        Intent clickIntent = new Intent(context, WidgetProvider.class);
//        clickIntent.setAction(context.getResources().getString(R.string.getOff_kor));
//        return PendingIntent.getBroadcast(context, 0, clickIntent, 0);
//    }
//
//    private RemoteViews addViews(Context context) {
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
//        views.setOnClickPendingIntent(R.id.btn_getOn, getOn(context));
//        views.setOnClickPendingIntent(R.id.btn_getOff, getOff(context));
//        return views;
//    }
//
//    @Override
//    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//        super.onUpdate(context, appWidgetManager, appWidgetIds);
//
//        RemoteViews views = addViews(context);
//        for (int appWidgetId : appWidgetIds) {
//            appWidgetManager.updateAppWidget(appWidgetId, views);
//        }
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        super.onReceive(context, intent);
//
//        String action = intent.getAction();
//        if (action.equals(context.getResources().getString(R.string.getOn_kor))) {
//            Common.showToast(context, "승차");
//        } else if (action.equals(context.getResources().getString(R.string.getOff_kor))) {
//            Common.showToast(context, "하차");
//        }
//    }
//}