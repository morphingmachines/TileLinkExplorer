package explorerTL.point2point

import chisel3._
import chisel3.util._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import org.chipsalliance.cde.config._

class MyClient1(implicit p: Parameters) extends LazyModule {
  val node = TLClientNode(
    Seq(TLMasterPortParameters.v1(Seq(TLMasterParameters.v1(name = "MyClient-1", sourceId = IdRange(0, 3))))),
  )
  val tlSrc                = InModuleBody(node.makeIOs())
  override lazy val module = new LazyModuleImp(this) {}
}

class MyManager1(implicit p: Parameters) extends LazyModule {
  val node = TLManagerNode(
    Seq(
      TLSlavePortParameters.v1(
        Seq(
          TLSlaveParameters.v1(
            address = Seq(AddressSet(0x20000, 0xffff)),
            regionType = RegionType.IDEMPOTENT,
            supportsGet = TransferSizes(4, 64),
            supportsPutFull = TransferSizes(4, 64),
          ),
        ),
        beatBytes = 4,
        minLatency = 1,
      ),
    ),
  )

  val tlSink               = InModuleBody(node.makeIOs())
  override lazy val module = new LazyModuleImp(this) {}
}

class Point2Point(implicit p: Parameters) extends LazyModule {
  val client1  = LazyModule(new MyClient1)
  val manager1 = LazyModule(new MyManager1)

  manager1.node := client1.node
  override lazy val module = new Point2PointImp(this)
}

class Point2PointImp(outer: Point2Point) extends LazyModuleImp(outer) {
  val io = IO(new Bundle {
    val in_a = Flipped(Decoupled(new TLBundleA(outer.client1.node.out(0)._1.params)))
    val in_d = Decoupled(new TLBundleD(outer.client1.node.out(0)._1.params))

    val out_a = Decoupled(new TLBundleA(outer.manager1.node.in(0)._1.params))
    val out_d = Flipped(Decoupled(new TLBundleD(outer.manager1.node.in(0)._1.params)))
  })

  outer.client1.tlSrc(0).a :<>= io.in_a
  io.out_a :<>= outer.manager1.tlSink(0).a
  io.in_d :<>= outer.client1.tlSrc(0).d
  outer.manager1.tlSink(0).d :<>= io.out_d
}
