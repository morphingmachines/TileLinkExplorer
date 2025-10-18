package explorerTL.tilelinkSwitchboard

import org.chipsalliance.cde.config.Parameters
import org.chipsalliance.diplomacy.lazymodule._

class TLLoopback(implicit p: Parameters) extends LazyModule with SwitchboardTLAdapter {

  override val nManager = 1
  override val nClient  = 1

  (0 until nManager).foreach(i => managers(i) := clients(i))

}
