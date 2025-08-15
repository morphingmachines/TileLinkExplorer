// See README.md for license details.

package explorerTL

import org.chipsalliance.cde.config.Parameters
import org.chipsalliance.diplomacy.lazymodule.LazyModule

/** To run from a terminal shell
  * {{{
  * mill explorerTL.runMain explorerTL.explorerTLMain
  * }}}
  */

object explorerTLMain extends App with emitrtl.LazyToplevel {
  val str = if (args.length == 0) "" else args(0)
  val lazyTop = str match {
    case "Point2Point" => LazyModule(new point2point.Point2Point()(Parameters.empty))
    case "RegNode"     => LazyModule(new regNode.DUT()(Parameters.empty))
    case "AsyncDevice" => LazyModule(new asyncDevice.DUT()(Parameters.empty))
    case _             => throw new Exception("Unknown Module Name!")
  }

  showModuleComposition(lazyTop)
  chisel2firrtl()
  firrtl2sv()
  genDiplomacyGraph()

}
