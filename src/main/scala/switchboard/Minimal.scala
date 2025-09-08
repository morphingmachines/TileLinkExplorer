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

class SwitchboardTest extends Module {
  val io = IO(new Bundle {
    val out = new SBIO
    val in  = Flipped(new SBIO)
  })

  val x = VecInit.tabulate(32)(i => io.in.bits.data(8 * (i + 1) - 1, 8 * i) + 1.U)

  io.out.valid     := io.in.valid
  io.in.ready      := io.out.ready
  io.out.bits.data := x.asUInt
  io.out.bits.dest := io.in.bits.dest
  io.out.bits.last := io.in.bits.last
}
