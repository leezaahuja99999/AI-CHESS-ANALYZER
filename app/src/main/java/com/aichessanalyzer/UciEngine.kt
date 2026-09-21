package com.aichessanalyzer

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

data class EngineInfo(val bestMove:String="",val depth:Int=0,val cp:Int?=null,val mate:Int?=null,val pv:String="")

class UciEngine(private val context:Context){
    private var p:Process?=null
    private var w:BufferedWriter?=null
    private var r:BufferedReader?=null

    private suspend fun ensure():Boolean=withContext(Dispatchers.IO){
        if(p?.isAlive==true)return@withContext true
        val dir=File(context.filesDir,"engine").apply{mkdirs()}
        val exe=File(dir,"stockfish")
        if(!exe.exists()){
            context.assets.open("stockfish/arm64-v8a/stockfish").use{a->FileOutputStream(exe).use{b->a.copyTo(b)}}
            exe.setExecutable(true)
        }
        p=ProcessBuilder(exe.absolutePath).redirectErrorStream(true).start()
        w=p!!.outputStream.bufferedWriter();r=p!!.inputStream.bufferedReader()
        send("uci"); waitToken("uciok",5000)
    }
    private fun send(s:String){w?.apply{write(s);newLine();flush()}}
    private fun waitToken(token:String,ms:Long){
        val t=System.currentTimeMillis()
        while(System.currentTimeMillis()-t<ms){
            if(r?.ready()==true){if(r!!.readLine()?.startsWith(token)==true)return}else Thread.sleep(10)
        }
    }
    suspend fun analyze(fen:String,depth:Int):EngineInfo=withContext(Dispatchers.IO){
        if(!ensure())return@withContext EngineInfo()
        send("isready");waitToken("readyok",5000);send("position fen $fen");send("go depth $depth")
        var info=EngineInfo()
        while(true){
            val line=r?.readLine()?:break
            if(line.startsWith("info "))info=parse(line,info)
            if(line.startsWith("bestmove ")){
                val b=line.split(" ").getOrNull(1)?:""
                return@withContext info.copy(bestMove=b)
            }
        }
        info
    }
    private fun parse(s:String,old:EngineInfo):EngineInfo{
        val a=s.split(" ");val d=a.indexOf("depth");val sc=a.indexOf("score");val pv=a.indexOf("pv")
        var cp=old.cp;var mate=old.mate
        if(sc>=0&&sc+2<a.size)if(a[sc+1]=="cp"){cp=a[sc+2].toIntOrNull();mate=null}else if(a[sc+1]=="mate"){mate=a[sc+2].toIntOrNull();cp=null}
        return old.copy(
            depth=if(d>=0)a.getOrNull(d+1)?.toIntOrNull()?:old.depth else old.depth,
            cp=cp,mate=mate,pv=if(pv>=0)a.drop(pv+1).joinToString(" ")else old.pv)
    }
    fun close(){try{send("quit")}catch(_:Exception){};p?.destroy();p=null}
}
