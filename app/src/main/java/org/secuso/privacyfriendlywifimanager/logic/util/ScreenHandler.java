/*
Copyright 2016-2018 Jan Henzel, Patrick Jauernig, Dennis Werner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.secuso.privacyfriendlywifimanager.logic.util;

import android.content.Context;

/**
 * Util functions regarding screen and density.
 */
public class ScreenHandler {

    /**
     * Converts dp to px
     * @param dp Density points.
     * @param context A context to use.
     * @return Pixels.
     */
    public static int getPXFromDP(int dp, Context context) {
        return (int) (context.getResources().getDisplayMetrics().density * dp);
    }
}
