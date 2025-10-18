package explorerTL.tilelinkSwitchboard

import freechips.rocketchip.diplomacy.AddressSet
import org.chipsalliance.cde.config.Parameters
import org.chipsalliance.diplomacy.lazymodule._

class TLMem(implicit p: Parameters) extends LazyModule with SwitchboardTLAdapter {

  override val nManager = 0
  override val nClient  = 1

  val ram = LazyModule(new freechips.rocketchip.tilelink.TLRAM(AddressSet(0, 0xffff), beatBytes = 4))
  ram.node := clients(0)

}
