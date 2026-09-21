package com.aichessanalyzer

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class BoardView(c:Context):View(c){
    val model=ChessModel(); private var sel:Pair<Int,Int>?=null
    private var arrow:String?=null
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    var flipped=false
    var onChange:(()->Unit)?=null
    private val u=mapOf("K" to "♔","Q" to "♕","R" to "♖","B" to "♗","N" to "♘","P" to "♙","k" to "♚","q" to "♛","r" to "♜","b" to "♝","n" to "♞","p" to "♟")
    fun setArrow(move:String){arrow=move;invalidate()}
    fun clearArrow(){arrow=null;invalidate()}
    override fun onDraw(c:Canvas){
        val s=width/8f
        for(y in 0..7)for(x in 0..7){
            val bx=if(flipped)7-x else x;val by=if(flipped)y else 7-y
            p.color=if((x+y)%2==0)Color.rgb(238,238,210)else Color.rgb(118,150,86)
            c.drawRect(x*s,y*s,x*s+s,y*s+s,p)
            if(sel==Pair(x,y)){p.color=Color.argb(120,30,144,255);c.drawRect(x*s,y*s,x*s+s,y*s+s,p)}
            val pc=model.pieceAt(bx,by)
            if(pc.isNotEmpty()){p.color=if(pc[0].isUpperCase())Color.WHITE else Color.BLACK;p.textAlign=Paint.Align.CENTER;p.textSize=s*.72f;c.drawText(u[pc]?:pc,x*s+s/2,y*s+s*.76f,p)}
        }
        arrow?.takeIf{it.length>=4}?.let{m->
            val a=xy(m.substring(0,2),s);val b=xy(m.substring(2,4),s)
            p.color=Color.rgb(255,193,7);p.strokeWidth=9f;p.style=Paint.Style.STROKE;c.drawLine(a.first,a.second,b.first,b.second,p);p.style=Paint.Style.FILL
        }
    }
    private fun xy(q:String,s:Float):Pair<Float,Float>{val f=q[0]-'a';val r=q[1]-'1';val x=if(flipped)7-f else f;val y=if(flipped)r else 7-r;return Pair(x*s+s/2,y*s+s/2)}
    override fun onTouchEvent(e:MotionEvent):Boolean{
        if(e.action!=MotionEvent.ACTION_UP)return true
        val s=width/8f;val sx=(e.x/s).toInt();val sy=(e.y/s).toInt()
        val f=if(flipped)7-sx else sx;val r=if(flipped)sy else 7-sy
        if(sel==null)sel=sx to sy else{
            val from="${('A'.code+(if(flipped)7-sel!!.first else sel!!.first)).toChar()}${(if(flipped)sel!!.second else 7-sel!!.second)+1}"
            val to="${('A'.code+f).toChar()}${r+1}"
            if(model.move(from,to)){sel=null;clearArrow();onChange?.invoke()}else sel=sx to sy
        };invalidate();return true
    }
}
