
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import _root_.circt.stage.ChiselStage
val dut = LazyModule(new explorerTL.Point2Point()(Parameters.empty))
ChiselStage.emitCHIRRTL(dut.module)
