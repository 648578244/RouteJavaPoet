package com.work.arouter_api.facade.service;

import android.net.Uri;

import com.work.arouter_api.facade.template.IProvider;

public interface PathReplaceService extends IProvider {

    String forString(String path);

    Uri forUri(Uri uri);
}
