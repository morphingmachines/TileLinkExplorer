package explorerTL.example6

import explorerTL.example6

import chiseltest._
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config.Parameters
import org.scalatest.freespec.AnyFreeSpec

/** This is a trivial example of how to run this Specification: From a terminal shell use:
  * {{{  
  * ./mill explorerTL.test.testOnly explorerTL.example6.tilelinkDiplomacy
  * }}}
  */
//class tilelinkDiplomacy extends AnyFreeSpec with ChiselScalatestTester {
class explorerTLSpec extends AnyFreeSpec with ChiselScalatestTester {

  "Test for reading from TLRAM in example 6" in {
    test(
      LazyModule(
        //new example5.AdderTestHarness()(new Config(new example5.DiplomacyExampleConfig)) with example5.HasOneNodeMonitor,
        new example6.MyTopLevel()(Parameters.empty),
      ).module,
    ).withAnnotations(
      Seq(
        WriteVcdAnnotation,
       // VerilatorBackendAnnotation, // Uncomment to use the Verilator backend
      ),
    ) {
      dut =>
        dut.io.readWrite.poke(true)
        dut.io.Valid.poke(true)
        dut.io.sID.poke(1)

        dut.clock.step(1)

        dut.io.Valid.poke(true)
        dut.io.readWrite.poke(false)
        dut.io.sID.poke(2)

         dut.clock.step(1)

         dut.io.Valid.poke(false)

        dut.clock.step(3)

         dut.io.readWrite.poke(false)
         dut.io.Valid.poke(true)
         dut.io.sID.poke(3)

        dut.clock.step(1)

        dut.io.Valid.poke(false)
        dut.clock.step(3)
    }
  }
}
