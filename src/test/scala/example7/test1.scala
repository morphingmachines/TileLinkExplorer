package explorerTL.example7

import explorerTL.example7

import chiseltest._
import freechips.rocketchip.diplomacy._
import org.chipsalliance.cde.config.Parameters
import org.scalatest.freespec.AnyFreeSpec

/** This is a trivial example of how to run this Specification: From a terminal shell use:
  * {{{
  * ./mill adder.test.testOnly adder.example2.DiplomaticAdder
  * ./mill explorerTL.test.testOnly explorerTL.example7.explorerTLSpec_example7
  * }}}
  */
class explorerTLSpec extends AnyFreeSpec with ChiselScalatestTester {

  "Test for example7" in {
    test(
      LazyModule(
        //new example5.AdderTestHarness()(new Config(new example5.DiplomacyExampleConfig)) with example5.HasOneNodeMonitor,
        new example7.MyTopLevel()(Parameters.empty),
      ).module,
    ).withAnnotations(
      Seq(
        WriteVcdAnnotation,
       // VerilatorBackendAnnotation, // Uncomment to use the Verilator backend
      ),
    ) {
      dut =>        
        //1
        dut.io.valid.poke(true)
        dut.io.id.poke(2)
        dut.io.dta.poke(45)
        dut.io.readWrite.poke(true)
        dut.io.logAddr.poke(2214604816L)
        dut.io.kerId.poke(4)

        dut.clock.step(1)

        //2
        dut.io.valid.poke(true)
        dut.io.id.poke(2)
        dut.io.dta.poke(13)
        dut.io.readWrite.poke(true)
        dut.io.logAddr.poke(2214604816L)
        dut.io.kerId.poke(4)

        dut.clock.step(1)

        //3
        dut.io.valid.poke(true)
        dut.io.id.poke(2)
        dut.io.dta.poke(12)
        dut.io.readWrite.poke(true)
        dut.io.logAddr.poke(2214604816L)
        dut.io.kerId.poke(4)

        dut.clock.step(1)

        //4
        dut.io.valid.poke(true)
        dut.io.id.poke(2)
        dut.io.dta.poke(10)
        dut.io.readWrite.poke(true)
        dut.io.logAddr.poke(2214604816L)
        dut.io.kerId.poke(4)

        dut.clock.step(1)
        
        dut.io.valid.poke(false)
        dut.io.id.poke(2)
        dut.io.logAddr.poke(736)
        dut.io.kerId.poke(4)
        dut.io.readWrite.poke(false)


        dut.io.readWrite.poke(true)
        dut.clock.step(2)

        dut.io.valid.poke(true)
        dut.io.id.poke(1)
        dut.io.dta.poke(10)
        dut.io.readWrite.poke(false)
        dut.io.logAddr.poke(2214604816L)
        dut.io.kerId.poke(4)

        dut.clock.step(1)
        
        dut.io.valid.poke(false)
        dut.clock.step(3)

        dut.io.valid.poke(true)
        dut.io.id.poke(4)
        dut.io.dta.poke(10)
        dut.io.readWrite.poke(true)
        dut.io.logAddr.poke(14604816L)
        dut.io.kerId.poke(4)

        dut.clock.step(1)

        dut.io.valid.poke(false)
        dut.clock.step(2)
        dut.clock.step(7)


        //KID+LA - 0x484003007 (in Hex)
    }
  }
}

