package com.rock.zxingphoto.utils;

import android.graphics.PointF;

/**
 * Created by Rock on 2018/9/3.
 */

public class ScanResult {
    public String result;
    PointF[] resultPoints;

    public ScanResult(String result) {
        this.result = result;
    }

    public ScanResult(String result, PointF[] resultPoints) {
        this.result = result;
        this.resultPoints = resultPoints;
    }
}

