package com.example.antonrynkovoy.androidminesweeper;
//Miner fo itStep
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{

    private GridLayout layoutGrid;
    private RelativeLayout layoutRel;
    private Button[][] button;
    private GameObject[][] gameObject;
    private TextView tvTimer;
    private int mines = 10;
    private float alpha = 100;

    private int flags = mines;

    public void restart(View view) {
        for (int i = 0; i < layoutGrid.getRowCount(); i++) {
            for (int j = 0; j < layoutGrid.getColumnCount(); j++) {
                gameObject[i][j] = GameObject.CELL;
                button[i][j].setBackgroundResource(R.drawable.shape_cell);
                button[i][j].setText("");
                sec = 0;
            }
        }
        generateMines();
    }


    private enum GameObject {
        CELL, MINE, MINE_DEAD, EMPTY, SOME_VALUE, FLAG, NOT_SURE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        layoutGrid = (GridLayout) findViewById(R.id.layoutGrid);
        layoutRel = (RelativeLayout) findViewById(R.id.layoutRel);
        tvTimer = (TextView) findViewById(R.id.tvTimer);

        layoutGrid.setColumnCount(9);
        layoutGrid.setRowCount(9);
        layoutGrid.setOrientation(GridLayout.HORIZONTAL);
        layoutRel.setBackgroundResource(R.drawable.ship_back);
        //layoutGrid.setBackgroundColor();

        button = new Button[layoutGrid.getColumnCount()][layoutGrid.getRowCount()];
        gameObject = new GameObject[layoutGrid.getColumnCount()][layoutGrid.getRowCount()];

        setFieldOption();
        generateMines();
        startTimer();
    }



    @Override
    public void onClick(View v) {

        boolean flag = false;

        for (int i = 0; i < layoutGrid.getColumnCount(); i++) {
            if (flag) {
                break;
            }
            for (int j = 0; j < layoutGrid.getRowCount(); j++) {
                if (button[i][j].getId() == v.getId()) {
                    try {
                        openCell(i, j);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Toast.makeText(this, MINES count: " + calcMine(i, j), Toast.LENGTH_SHORT).show();
                }
            }
        }


        boolean isWin = false;
        boolean isBreak = false;

        for (int i = 0; i < layoutGrid.getRowCount(); i++) {
            if (isBreak) {
                break;
            }
            for (int j = 0; j < layoutGrid.getColumnCount(); j++) {
                if (gameObject[i][j] != GameObject.CELL) {
                    isWin = true;
                } else {
                    isWin = false;
                    isBreak = true;
                    break;
                }
            }
        }

        if (isWin) {
            openDialog();
        }

    }

    @Override
    public boolean onLongClick(View v) {
        boolean flag = false;

        for (int i = 0; i < layoutGrid.getColumnCount(); i++) {
            if (flag) {
                break;
            }
            for (int j = 0; j < layoutGrid.getRowCount(); j++) {
                if (button[i][j].getId() == v.getId()) {
                    if (gameObject[i][j] == GameObject.CELL) {
                        if(flags > 0) {
                            //labelFlagsCount.setText(--flags + "");
                            button[i][j].setBackgroundResource(R.drawable.shape_flag);
                            gameObject[i][j] = GameObject.FLAG;
                            if (gameObject[i][j] == GameObject.FLAG) {
                                //Toast.makeText(this, "Set flag from cell", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else if (gameObject[i][j] == GameObject.MINE) {
                        if(flags > 0) {
                            mineY.add(i);
                            mineX.add(j);
                            button[i][j].setBackgroundResource(R.drawable.shape_flag);
                            //if (  board[row][coll] == GameObject.FLAG) System.out.println("SET FLAG from mine");
                            gameObject[i][j] = GameObject.FLAG;
                            if (gameObject[i][j] == GameObject.FLAG) {
                                //Toast.makeText(this, "Set flag from mine", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }else if (gameObject[i][j] == GameObject.FLAG) {
                        //labelFlagsCount.setText(++flags + "");
                        button[i][j].setBackgroundResource(R.drawable.shape_question);
                        gameObject[i][j] = GameObject.NOT_SURE;
                    } else if( gameObject[i][j] == GameObject.NOT_SURE) {
                        button[i][j].setBackgroundResource(R.drawable.shape_cell);
                        gameObject[i][j] = GameObject.CELL;
                        for (int ii = 0; i < mineY.size(); i++) {
                            if (mineY.get(ii) == i && mineX.get(ii) == j) {
                                gameObject[i][j] = GameObject.MINE;
                                //Toast.makeText(this, i+":"+j + " = MINE", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    //Toast.makeText(this, "LOONG + " + i + ":" + j, Toast.LENGTH_SHORT).show();
                }
            }

        }
        return false;
    }

    private void setFieldOption() {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        for (int i = 0; i < layoutGrid.getColumnCount(); i++) {
            for (int j = 0; j < layoutGrid.getRowCount(); j++) {
                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
                button[i][j] = new Button(this);
                //gridParams.setMargins(5, 5, 5, 5);
                gridParams.width = displaymetrics.widthPixels / layoutGrid.getColumnCount();
                gridParams.height = gridParams.width;

                button[i][j].setBackgroundResource(R.drawable.shape_cell);
                button[i][j].setLayoutParams(gridParams);
                button[i][j].setId(Integer.parseInt(i + "" + j));

                layoutGrid.addView(button[i][j]);

                button[i][j].setOnClickListener(this);
                button[i][j].setOnLongClickListener(this);
                gameObject[i][j] = GameObject.CELL;
            }
        }
    }

    private void generateMines() {
        for (int i = 0; i < mines; i++) {
            boolean flag = true;

            while (flag == true) {
                int randomI = (int) (Math.random() * layoutGrid.getColumnCount());
                int randomJ = (int) (Math.random() * layoutGrid.getRowCount());

                if (gameObject[randomI][randomJ] == GameObject.CELL) {
                    //button[randomI][randomJ].setBackgroundResource(R.drawable.mine);
                    gameObject[randomI][randomJ] = GameObject.MINE;
                    flag = false;
                }
            }
        }
    }

      private int calcMine(int i, int j) {
        int mineCount = 0;
        if (i != 0 && j != 0 && gameObject[i - 1][j - 1] == GameObject.MINE) mineCount++;
        if (i != layoutGrid.getRowCount() - 1 && j != layoutGrid.getColumnCount() - 1 && gameObject[i + 1][j + 1] == GameObject.MINE)
            mineCount++;
        if (i != layoutGrid.getRowCount() - 1 && j != 0 && gameObject[i + 1][j - 1] == GameObject.MINE)
            mineCount++;
        if (i != 0 && j != layoutGrid.getColumnCount() - 1 && gameObject[i - 1][j + 1] == GameObject.MINE)
            mineCount++;
        if (j != layoutGrid.getColumnCount() - 1 && gameObject[i][j + 1] == GameObject.MINE)
            mineCount++;
        if (j != 0 && gameObject[i][j - 1] == GameObject.MINE) mineCount++;
        if (i != layoutGrid.getRowCount() - 1 && gameObject[i + 1][j] == GameObject.MINE) mineCount++;
        if (i != 0 && gameObject[i - 1][j] == GameObject.MINE) mineCount++;
        return mineCount;
    }

    private ArrayList<Integer> mineX = new ArrayList<>();
    private ArrayList<Integer> mineY = new ArrayList<>();

    void openCell(int i, int j) throws InterruptedException {
        if (gameObject[i][j] == GameObject.MINE) {
            //layoutGrid.setStyle("-fx-background-color: red");
            gameObject[i][j] = GameObject.MINE_DEAD;
            button[i][j].setBackgroundResource(R.drawable.shape_cellbombdead);
            for (int k = 0; k < layoutGrid.getColumnCount(); k++) {
                for (int l = 0; l < layoutGrid.getRowCount(); l++) {
                    if (gameObject[k][l] == GameObject.MINE) {
                        button[k][l].setBackgroundResource(R.drawable.shape_cellbomb);
                    }
                }
            }
           openDialog();
            for (int k = 0; k < mineX.size(); k++) {
                button[mineY.get(k)][mineX.get(k)].setBackgroundResource(R.drawable.mine_flag);
            }
        } else if (gameObject[i][j] == GameObject.CELL) { // is NORMAL, and not EMPTY!
            int result = calcMine(i, j);
            if (result == 0) {
                gameObject[i][j] = GameObject.EMPTY;
                button[i][j].setBackgroundResource(R.drawable.shape_opencell);
                for (int k = i - 1; k <= i + 1; k++) {
                    for (int l = j - 1; l <= j + 1; l++) {
                        if (l >= 0 && l < layoutGrid.getRowCount() && k >= 0 && k < layoutGrid.getColumnCount()) {
                            try {
                                openCell(k, l);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                gameObject[i][j] = GameObject.SOME_VALUE;
                button[i][j].setText(result+"");
                button[i][j].setTextSize(18);
                switch (result) {
                    case 0:
                        button[i][j].setTextColor(Color.BLACK);
                        break;
                    case 1:
                        button[i][j].setTextColor(Color.GREEN);
                        break;
                    case 2:
                        button[i][j].setTextColor(Color.rgb(0, 255, 255));
                        break;
                    case 3:
                        button[i][j].setTextColor(Color.rgb(128, 0, 128));
                        break;
                    case 4:
                        button[i][j].setTextColor(Color.rgb(255, 255, 0));
                        break;
                    case 5:
                        button[i][j].setTextColor(Color.rgb(255, 128, 0));
                        break;
                    case 6:
                        button[i][j].setTextColor(Color.rgb(128, 128, 0));
                        break;
                    case 7:
                        button[i][j].setTextColor(Color.rgb(255, 0, 128));
                        break;
                    case 8:
                        button[i][j].setTextColor(Color.RED);
                        break;
                }
            }
        }
    }

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("**Congratulations**")
                .setMessage("*You win*")
                .setIcon(R.drawable.smile)
                .setCancelable(false)
                .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restart(null);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private int sec = 0;
    private void startTimer(){
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                tvTimer.setText(++sec+"");
                handler.postDelayed(this, 1000);
            }
        });
    }
}
