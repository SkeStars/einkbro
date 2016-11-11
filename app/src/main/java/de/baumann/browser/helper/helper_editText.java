/*
    This file is part of the Browser WebApp.

    Browser WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Browser WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Browser webview app.

    If not, see <http://www.gnu.org/licenses/>.
 */

package de.baumann.browser.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.baumann.browser.R;
import de.baumann.browser.databases.Database_Bookmarks;
import de.baumann.browser.databases.Database_Pass;
import de.baumann.browser.popups.Popup_bookmarks;
import de.baumann.browser.popups.Popup_readLater;

public class helper_editText {


    public static void editText_Touch(EditText editText, final Activity from) {

        editText.setOnTouchListener(new class_OnSwipeTouchListener_editText(from) {
            public void onSwipeTop() {
                from.finishAffinity();
            }
            public void onSwipeRight() {
                helper_main.switchToActivity(from, Popup_readLater.class, "", false);
            }
            public void onSwipeLeft() {
                helper_main.switchToActivity(from, Popup_bookmarks.class, "", false);
            }
        });
    }

    public static void editText_EditorAction(final EditText editText, final Activity from, final WebView webView) {

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);
        final String searchEngine = sharedPref.getString("searchEngine", "https://startpage.com/do/search?query=");
        final String wikiLang = sharedPref.getString("wikiLang", "en");

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ( (actionId == EditorInfo.IME_ACTION_SEARCH) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN ))){
                    String text = editText.getText().toString();

                    if (text.length() > 3) {
                        String subStr=text.substring(3);

                        if (text.contains("http")) {
                            webView.loadUrl(text);
                        } else if (text.contains(".w ")) {
                            webView.loadUrl("https://" + wikiLang + ".wikipedia.org/wiki/Spezial:Suche?search=" + subStr);
                        } else if (text.startsWith(".f ")) {
                            webView.loadUrl("https://www.flickr.com/search/?advanced=1&license=2%2C3%2C4%2C5%2C6%2C9&text=" + subStr);
                        } else  if (text.startsWith(".m ")) {
                            webView.loadUrl("https://metager.de/meta/meta.ger3?focus=web&eingabe=" + subStr);
                        } else if (text.startsWith(".g ")) {
                            webView.loadUrl("https://github.com/search?utf8=✓&q=" + subStr);
                        } else  if (text.startsWith(".s ")) {
                            webView.loadUrl("https://startpage.com/do/search?query=" + subStr);
                        } else if (text.startsWith(".G ")) {
                            webView.loadUrl("https://www.google.com/search?&q=" + subStr);
                        } else  if (text.startsWith(".y ")) {
                            webView.loadUrl("https://www.youtube.com/results?search_query=" + subStr);
                        } else  if (text.startsWith(".d ")) {
                            webView.loadUrl("https://duckduckgo.com/?q=" + subStr);
                        } else {
                            webView.loadUrl(searchEngine + text);
                        }
                    }

                    editText.setText(text);
                    editText.clearFocus();
                    InputMethodManager imm = (InputMethodManager)from.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public static void editText_saveBookmark(final EditText editText, final Activity from, final WebView webView) {

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);
        sharedPref.edit()
                .putInt("keyboard", 2)
                .apply();
        from.invalidateOptionsMenu();

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                editText.requestFocus();
                helper_main.showKeyboard(from, editText);
                editText.setText(webView.getTitle());
            }
        }, 200);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ( (actionId == EditorInfo.IME_ACTION_SEARCH) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN ))){

                    try {

                        final Database_Bookmarks db = new Database_Bookmarks(from);
                        String inputTag = editText.getText().toString().trim();

                        db.addBookmark(inputTag, webView.getUrl());
                        db.close();
                        Snackbar.make(webView, R.string.bookmark_added, Snackbar.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    editText.setText(webView.getTitle());
                    editText.clearFocus();
                    InputMethodManager imm = (InputMethodManager)from.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);
                    sharedPref.edit()
                            .putInt("keyboard", 0)
                            .apply();
                    from.invalidateOptionsMenu();

                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public static void editText_savePass(final Activity from, final WebView webView) {

        final class_SecurePreferences sharedPrefSec = new class_SecurePreferences(from, "sharedPrefSec", "Ywn-YM.XK$b:/:&CsL8;=L,y4", true);

        try {
            final LinearLayout layout = new LinearLayout(from);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER_HORIZONTAL);
            final EditText input = new EditText(from);
            input.setSingleLine(true);
            input.setHint(R.string.pass_userName);
            final EditText input2 = new EditText(from);
            input2.setSingleLine(true);
            input2.setHint(R.string.pass_userPW);
            layout.setPadding(30, 0, 50, 0);
            layout.addView(input);
            layout.addView(input2);

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    helper_main.showKeyboard(from,input);
                }
            }, 200);

            final Database_Pass db = new Database_Pass(from);
            final AlertDialog.Builder dialog = new AlertDialog.Builder(from)
                    .setView(layout)
                    .setMessage(from.getString(R.string.pass_message) + " " + webView.getTitle())
                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                            String inputTag = input.getText().toString().trim();
                            String inputTag2 = input2.getText().toString().trim();

                            sharedPrefSec.put(webView.getUrl() + "UN", inputTag);
                            sharedPrefSec.put(webView.getUrl() + "PW", inputTag2);

                            db.addBookmark(webView.getTitle(), webView.getUrl(),
                                    sharedPrefSec.getString(webView.getUrl() + "UN"),
                                    sharedPrefSec.getString(webView.getUrl() + "PW"));
                            db.close();
                            Snackbar.make(webView, R.string.pass_success, Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void editText_searchSite (final EditText editText, final Activity from, final WebView webView) {

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(from);
        sharedPref.edit()
                .putInt("keyboard", 1)
                .apply();
        from.invalidateOptionsMenu();

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                editText.requestFocus();
                editText.setText("");
                editText.setHint(R.string.app_search_hint_site);
                helper_main.showKeyboard(from, editText);
            }
        }, 200);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_SEARCH) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN ))){
                    String text = editText.getText().toString();
                    editText.setText(from.getString(R.string.app_search) + " " + text);
                    webView.findAllAsync(text);
                    editText.clearFocus();
                    helper_main.hideKeyboard(from, editText);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    public static void editText_FocusChange(final EditText editText, final Activity from) {

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editText.setText("");
                    InputMethodManager imm = (InputMethodManager) from.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    InputMethodManager imm = (InputMethodManager)from.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
        });
    }
}
