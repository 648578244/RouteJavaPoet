package com.work.arouter_api.facade.service;

import android.content.Context;

import com.work.arouter_api.facade.Postcard;
import com.work.arouter_api.facade.template.IProvider;

public interface PretreatmentService extends IProvider {
    /**
     *  Do something before navigation.
     * @param context
     * @param postcard
     * @return
     */
    boolean onPretreatment(Context context, Postcard postcard);

}
