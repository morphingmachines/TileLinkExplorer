package explorerTL.example6

import chisel3._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import org.chipsalliance.cde.config._

class MyTopLevel()(implicit p: Parameters) extends LazyModule {
  println(s"Begin Test-Harness $p")

  val ram = LazyModule(new TLRAM(AddressSet(0x0,   0x3ff), beatBytes = 8, sramReg = false))
  val master = LazyModule(new MyMaster("MasterName"))
  ram.node := master.node
  lazy val module = new MyTopLevelImp(this)
  override lazy val desiredName: String = "MyTopLevel"
 println(s"End Test-Harness $p")
}

class MyTopLevelImp(outer: MyTopLevel) extends LazyModuleImp(outer) {
  val io = IO(new Bundle {
    val readWrite = Input(Bool())
    val Valid = Input(Bool())
    val sID = Input(UInt(5.W))
  })
  outer.master.module.io.rw := io.readWrite
  outer.master.module.io.valid := io.Valid
  outer.master.module.io.srcID := io.sID  
}

class MyMaster(name: String)(implicit p: Parameters) extends LazyModule {
  
  val node = TLClientNode(Seq(TLMasterPortParameters.v1(Seq(TLMasterParameters.v1(name = name, sourceId = IdRange(0, 4))))))
  
  lazy val module = new Impl

  //Implementation of RTL in class Impl(LazyModuleImp)
  
  class Impl extends LazyModuleImp(this) {
    val io = IO(new Bundle { 
      val rw = Input(Bool())
      val dataOut = Output(UInt(5.W))
      val valid = Input(Bool())
      val srcID = Input(UInt(5.W))
    })
    val addr = 8.U
    val size = 2.U
    val (tl, edgeOut) = node.out(0)
    val (sr, pbits) = edgeOut.Put(io.srcID, addr, size, 15.U) //Write
    val (sr1, gbits) = edgeOut.Get(io.srcID, addr, size) //Read
     tl.a.bits := Mux(io.rw, pbits,gbits)
    tl.a.valid := Mux(io.valid, true.B, false.B)
    tl.d.ready := true.B


    when(tl.d.fire) {
      val resp = tl.d.bits
      when(resp.opcode === TLMessages.AccessAckData) {
        io.dataOut := 1.U //read
      }.elsewhen(resp.opcode === TLMessages.AccessAck) {
        io.dataOut := 2.U // write
      }.otherwise {
        io.dataOut := 0.U 
      }
    }.otherwise {
      io.dataOut := 0.U 
    }

  printf(cf"Final Output = ${io.dataOut}\n")
  printf(cf"Tilelink A Channel Bits = ${tl.a.bits}\n")
  printf(cf"Tilelink D Channel Bits = ${tl.d.bits}\n")
  printf("---------------------\n")
  }
}


