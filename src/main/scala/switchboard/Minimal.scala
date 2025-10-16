package explorerTL.switchboard

import chisel3._
import chisel3.util._
//import freechips.rocketchip.diplomacy.{AddressSet, IdRange, RegionType, TransferSizes}
//import freechips.rocketchip.tilelink._
//import org.chipsalliance.cde.config._
//import org.chipsalliance.diplomacy.lazymodule.{InModuleBody, LazyModule, LazyModuleImp}

class SwitchboardIfc extends Bundle {
  val data = Output(UInt(256.W))
  val dest = Output(UInt(32.W))
  val last = Output(Bool())
}

class SBIO extends IrrevocableIO[SwitchboardIfc](new SwitchboardIfc)

class Minimal extends Module {
  val io = IO(new Bundle {
    val out = new SBIO
    val in  = Flipped(new SBIO)
  })

  val buf = Module(new Queue(new SwitchboardIfc, 32))

  val x = VecInit(io.in.bits.data.asTypeOf(Vec(32, UInt(8.W))).map(i => i + 1.U))
  buf.io.enq.valid     := io.in.valid
  buf.io.enq.bits.data := x.asUInt
  buf.io.enq.bits.dest := io.in.bits.dest
  buf.io.enq.bits.last := io.in.bits.last
  io.in.ready          := buf.io.enq.ready
  io.out <> buf.io.deq

}
