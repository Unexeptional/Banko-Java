/*
 * Copyright (c) 2014 Oleksandr Tyshkovets <olexandr.tyshkovets@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, color_software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * CALL IT DONE 1.11.2018
 * ehm kind of copied
 */
package com.unexceptional.beast.banko.newVersion.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.unexceptional.beast.banko.R;


/**
 * Soft numeric keyboard for lock screen and passcode preference.
 * @author Oleksandr Tyshkovets <olexandr.tyshkovets@gmail.com>
 */
public class KeyboardFragment extends Fragment {

    private static final int DELAY = 500;

    private int pass1;
    private int pass2;
    private int pass3;
    private int pass4;
    private ImageView pass_dot1;
    private ImageView pass_dot2;
    private ImageView pass_dot3;
    private ImageView pass_dot4;

    private boolean detachFlag=true;

    private int length = 0;

    public interface OnPassEnteredListener {
        void onPassEntered(String pass);
        void onDissmissed();
    }

    private OnPassEnteredListener listener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_numeric_keyboard, container, false);

        LinearLayout background= rootView.findViewById(R.id.background);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        pass_dot1 = rootView.findViewById(R.id.pass_dot1);
        pass_dot2 = rootView.findViewById(R.id.pass_dot2);
        pass_dot3 = rootView.findViewById(R.id.pass_dot3);
        pass_dot4 = rootView.findViewById(R.id.pass_dot4);

        rootView.findViewById(R.id.one_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(1);
            }
        });
        rootView.findViewById(R.id.two_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(2);
            }
        });
        rootView.findViewById(R.id.three_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { add(3);
            }
        });
        rootView.findViewById(R.id.four_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(4);
            }
        });
        rootView.findViewById(R.id.five_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(5);
            }
        });
        rootView.findViewById(R.id.six_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(6);
            }
        });
        rootView.findViewById(R.id.seven_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { add(7);
            }
        });
        rootView.findViewById(R.id.eight_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { add(8);
            }
        });
        rootView.findViewById(R.id.nine_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(9);
            }
        });
        rootView.findViewById(R.id.zero_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add(0);
            }
        });

        rootView.findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (length) {
                    case 1:
                        pass1=0;
                        pass_dot1.setImageResource(R.drawable.outline_lens_black_24);
                        length--;
                        break;
                    case 2:
                        pass2=0;
                        pass_dot2.setImageResource(R.drawable.outline_lens_black_24);
                        length--;
                        break;
                    case 3:
                        pass3=0;
                        pass_dot3.setImageResource(R.drawable.outline_lens_black_24);
                        length--;
                        break;
                    case 4:
                        pass4=0;
                        pass_dot4.setImageResource(R.drawable.outline_lens_black_24);
                        length--;
                }
            }
        });

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnPassEnteredListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement "
                    + OnPassEnteredListener.class);
        }
    }

    private void add(int num) {
        switch (length + 1) {
            case 1:
                pass1=num;
                pass_dot1.setImageResource(R.drawable.baseline_lens_black_24);
                length++;
                break;
            case 2:
                pass2=num;
                pass_dot2.setImageResource(R.drawable.baseline_lens_black_24);
                length++;
                break;
            case 3:
                pass3=num;
                pass_dot3.setImageResource(R.drawable.baseline_lens_black_24);
                length++;
                break;
            case 4:
                pass4=num;
                pass_dot4.setImageResource(R.drawable.baseline_lens_black_24);
                length++;

                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        detachFlag=false;
                        listener.onPassEntered(String.valueOf(pass1) + String.valueOf(pass2) +
                                        String.valueOf(pass3) + String.valueOf(pass4));
                        pass1=0;
                        pass2=0;
                        pass3=0;
                        pass4=0;
                        pass_dot1.setImageResource(R.drawable.outline_lens_black_24);
                        pass_dot2.setImageResource(R.drawable.outline_lens_black_24);
                        pass_dot3.setImageResource(R.drawable.outline_lens_black_24);
                        pass_dot4.setImageResource(R.drawable.outline_lens_black_24);
                        length = 0;
                    }
                }, DELAY);
        }
    }

    @Override
    public void onDetach() {
        if(detachFlag)
            listener.onDissmissed();
        super.onDetach();
    }

}
