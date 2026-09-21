package com.aichessanalyzer

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*

class MainActivity:AppCompatActivity(){
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    private lateinit var board:BoardView;private lateinit var eval:TextView;private lateinit var info:TextView
    private lateinit var engine:UciEngine;private var depth=18
    override fun onCreate(b:Bundle?){super.onCreate(b);engine=UciEngine(this)
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(12,12,12,12);setBackgroundColor(Color.rgb(13,17,23))}
        root.addView(TextView(this).apply{text="♟  AI CHESS ANALYZER";textSize=23f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setPadding(0,8,0,12)})
        board=BoardView(this);root.addView(board,LinearLayout.LayoutParams(-1,0,1f))
        eval=TextView(this).apply{text="Evaluation  0.00";textSize=21f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setPadding(0,8,0,2)};root.addView(eval)
        info=TextView(this).apply{text="Depth 18 • Best move —";textSize=14f;gravity=Gravity.CENTER;setTextColor(Color.LTGRAY);setPadding(0,2,0,8)};root.addView(info)
        val controls=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        fun btn(t:String,action:()->Unit)=Button(this@MainActivity).apply{text=t;setOnClickListener{action()}}
        controls.addView(btn("UNDO"){if(board.model.undo()){board.clearArrow();board.invalidate()}})
        controls.addView(btn("REDO"){if(board.model.redo()){board.clearArrow();board.invalidate()}})
        controls.addView(btn("FLIP"){board.flipped=!board.flipped;board.invalidate()})
        controls.addView(btn("NEW"){board.model.reset();board.clearArrow();board.invalidate()})
        root.addView(controls)
        val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        row.addView(btn("ANALYZE"){
            eval.text="Thinking…";info.text="Stockfish is calculating at depth $depth"
            scope.launch{
                val r=engine.analyze(board.model.fen(),depth)
                val score=r.mate?.let{"Mate $it"}?:r.cp?.let{String.format("%.2f",it/100.0)}?:"—"
                eval.text="Evaluation  $score";info.text="Depth ${r.depth} • Best move ${r.bestMove.ifBlank{"—"}}  •  PV ${r.pv.ifBlank{"—"}}"
                if(r.bestMove.isNotBlank())board.setArrow(r.bestMove)
            }
        },LinearLayout.LayoutParams(0,-2,1f))
        row.addView(btn("DEPTH"){
            depth=when(depth){12->18;18->24;24->30;else->12};info.text="Depth set to $depth"
        },LinearLayout.LayoutParams(0,-2,1f))
        row.addView(btn("FEN"){
            val e=EditText(this);e.hint="Paste FEN";e.setSingleLine()
            AlertDialog.Builder(this).setTitle("Load FEN").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Load"){_,_->if(board.model.loadFen(e.text.toString())){board.clearArrow();board.invalidate();eval.text="Evaluation —";info.text="FEN loaded"}}.show()
        },LinearLayout.LayoutParams(0,-2,1f))
        root.addView(row)
        board.onChange={eval.text="Evaluation —";info.text="Position changed • Tap ANALYZE"}
        setContentView(root)
    }
    override fun onDestroy(){scope.cancel();engine.close();super.onDestroy()}
}
