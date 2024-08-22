package explorerTL.example7

import chisel3._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import org.chipsalliance.cde.config._
import chisel3.util._

//top-level class for connecting nodes
class MyTopLevel()(implicit p: Parameters) extends LazyModule {
  println(s"Begin Test-Harness $p")
  val ram = LazyModule(new TLRAM(AddressSet(0x000000,   0x3ffffff), beatBytes = 4, sramReg = false))
  val mastergen = LazyModule(new MasterGen)
  val mmu = LazyModule(new MyMMU)
  //Make connection between all nodes here
  ram.node  := TLFragmenter(4,64) := mmu.masterNode
  mmu.slaveNode := mastergen.node 
  //mmu.slaveNode := mastergen.node 
  lazy val module = new MyTopLevelImp(this)
  override lazy val desiredName: String = "MyTopLevel"
  println(s"End Test-Harness $p")
}

class MyTopLevelImp(outer: MyTopLevel) extends LazyModuleImp(outer) {
  val io = IO(new Bundle {
    val logAddr = Input(UInt(32.W))
    val kerId = Input(UInt(5.W))
    val readWrite = Input(Bool())
    val id = Input(UInt(5.W))
    val cnt = Input(UInt(5.W))
    val dta = Input(UInt(64.W))
    val valid = Input(Bool())
  })
  outer.mastergen.module.io.LA := io.logAddr
  outer.mastergen.module.io.kID := io.kerId
  outer.mastergen.module.io.rw := io.readWrite
  outer.mastergen.module.io.srId := io.id
  outer.mastergen.module.io.valid := io.valid
  outer.mastergen.module.io.dataIn := io.dta
}

//MMU RTL consisting of 1 master and 1 slave node
class MyMMU(implicit p: Parameters) extends LazyModule {

    val device = new SimpleDevice("my-device", Seq("tutorial,my-device0"))
    val beatBytes = 4

    //Master node to talk to the RAM
    val masterNode = TLClientNode(Seq(TLMasterPortParameters.v1(Seq(TLMasterParameters.v1(name = name, sourceId = IdRange(0, 8))))))

    //slave node for getting translation requests
    val slaveNode = TLManagerNode(Seq(TLSlavePortParameters.v1(Seq(TLSlaveParameters.v1(
    address = Seq(AddressSet(0x00000000000L, 0xffffffffffL)),
    resources = device.reg,
    regionType = RegionType.UNCACHED,
    executable = true,
    supportsArithmetic = TransferSizes(1, beatBytes),
    supportsLogical = TransferSizes(1, beatBytes),
    supportsGet = TransferSizes(1, 64),
    supportsPutFull = TransferSizes(1, 64),
    supportsPutPartial = TransferSizes(1, 64),
    supportsHint = TransferSizes(1, beatBytes),
    fifoId = Some(0))), beatBytes)))
//Make slave and master Nodes
    lazy val module = new Impl

    class Impl extends LazyModuleImp(this) {
        //Declaration of Slave and Master nodes of the MMU
        val (in, edgeIn) = slaveNode.in(0)
        val (out,edgeOut) = masterNode.out(0)
        val dec =  RegInit(false.B)
        //in.d.valid := out.d.valid
        //val a_last = edge.last(a)
        //when(in.a.fire)
        //{
        //    in.d.bits := edgeIn.AccessAck(in.a.bits, 25.U)
        //    printf(cf" Output0 = ${in.d.bits}\n")
        //}   
    //in.d.bits.opcode := Mux(hasData, TLMessages.AccessAck, TLMessages.AccessAckData)
    printf(cf" Slave1 D Valid = ${in.d.valid}\n")
    printf(cf" Slave1 D Ready = ${in.d.ready}\n")
    printf(cf" first byte = ${edgeIn.first(out.a)}\n")

    val x = RegInit(0.U(70.W))
    //val y = RegInit(0.U(70.W))
    val y = Wire(UInt(70.W))
    val dataInR0 = 20.U         //Base Address + Segment size + Valid for ATT R0
    val dataInR1 = 163857.U         //Base Address + Segment size + Valid for ATT R1
    val memR0write = 13.U       //Selector bits for ATT R0 
    val memR1write = 129.U       //Selector bits for ATT R1
    val decider1 = RegInit(false.B)
    //

    //Pipelines initialization
    val buf = Vector.fill(2)(Module(new Queue(UInt(70.W),1, true)))
    for(i <- 0 until 2){
       buf(i).io.enq.valid := false.B
       buf(i).io.enq.bits := 0.U
       buf(i).io.deq.ready := false.B
    }
    val pipeline = Vector.fill(2)(Module(new Queue(new TLBundleA(edgeIn.bundle), 1, true)))
    for(i <- 0 until 2){
    pipeline(i).io.enq.valid := false.B
    pipeline(i).io.deq.ready := false.B
    }
    val logicalAddress = pipeline(0).io.deq.bits.address(31,0)
    val kernelId = pipeline(0).io.deq.bits.address(36,32)
    printf(cf" buff 0 enq v = ${pipeline(0).io.enq.valid}\n")
    val lock = Module(new Queue(Bool(),1, true))
    lock.io.enq.valid := false.B
    lock.io.enq.bits := false.B
    lock.io.deq.ready := false.B

    //connecting the ready signals
    in.a.ready := pipeline(0).io.enq.ready
    pipeline(0).io.enq.valid := in.a.valid

    pipeline(1).io.deq.ready := out.a.ready
    //out.a.valid := pipeline(1).io.deq.valid
    
    def addressGen(la : UInt)={
        val outData = Mux(la(31), Cat(la(36,32), la(30,26)),  Cat(la(36,32), la(28,25)))
        printf(cf" outdata = ${outData}\n")
        outData
    }

    def comparator(inpData:UInt, refData :UInt) = {
        val ans = Mux(inpData > refData, 2.U, 3.U)  
        ans
    }

    def combo(laData : UInt, attData : UInt)={ //when flag = 1, ie, ladata[31] === 1.U, i.e. Table R1
        val res = Mux(attData(0) === 1.U, Mux(laData(31) === 1.U, comparator(laData(25,12) , attData(14, 1)), comparator(laData(24,12), attData(13, 1))), 1.U)
        printf(cf" res = ${res}\n")
        printf(cf" la1 = ${attData}\n")
        val opRes = Wire(UInt(64.W))
        when(res === 3.U)
        {
            opRes := Cat(Cat(Mux(laData(31) === 1.U, laData(25,12) + attData(66,15), laData(24,12) + attData(65,14)), laData(11,0)), res)
            //decider1 := true.B
        }.otherwise
        {
            opRes := Cat(0.U, res)
            //dec := true.B
            printf(cf" dec Valid = ${dec}\n")
        }
        opRes
    }
    val memr0 = Mem(512, UInt(66.W))  // Table R0 => Width - Base Address[51:0] + Segment size(LA[24:12]) + Valid
    val memr1 = Mem(1024, UInt(67.W)) // Tabke R1 => Width - Base Address[51:0] + Segment size(LA[25:12]) + Valid

    memr0(memR0write):= dataInR0
    memr1(memR1write):= dataInR1

    val inpData = RegInit(0.U(32.W))
    val valReg = RegInit(false.B)
    val hasdta = RegInit(false.B)
    val condition = RegInit(false.B)

    pipeline(0).io.enq.bits := in.a.bits
    when(in.a.fire)
    {
        //pipeline.io.enq.valid := true.B
        //buf(0).io.enq.bits := Cat(Mux(logicalAddress(31), memr1(addressGen(logicalAddress)), memr0(addressGen(logicalAddress))),edgeIn.hasData(in.a.bits))
        //pipeline.io.enq.bits := Cat(edgeIn.hasData(in.a.bits),in.a.bits.data, logicalAddress)
        x := Mux(in.a.bits.address(31), memr1(addressGen(in.a.bits.address)), memr0(addressGen(in.a.bits.address)))

        //when(buf(0).io.enq.bits(1,0) == )
        //hasdta := edgeIn.hasData(in.a.bits)
        //inpData := logicalAddress
        //valReg := true.B
         
    }.otherwise
    {
        //pipeline.io.enq.valid := false.B
        //pipeline.io.enq.bits := 0.U
        x := 0.U 
        //inpData := 0.U
        //valReg:= false.B
    }
    printf(cf" buff 0 deqv = ${pipeline(0).io.deq.valid}\n")
    printf(cf" buff 0 = ${pipeline(0).io.enq.bits}\n")
    
    y := combo(pipeline(0).io.deq.bits.address(31,0), x)
    val physicalAddress = y(65, 2)
    val responseId = y(1,0)
    //out.d.ready := true.B
    val arbiter = Module(new Arbiter(new TLBundleD(edgeIn.bundle), 2)) 
    //var busy = false.B
    //in.d.bits := Mux(condition, out.d.bits, buf(0).io.deq.bits)
    //in.d.valid := Mux(condition, out.d.valid, buf(0).io.deq.valid)
    arbiter.io.in(1).valid := out.d.valid
    arbiter.io.in(1).bits := out.d.bits
    out.d.ready := arbiter.io.in(1).ready

    arbiter.io.in(0).valid := Mux(responseId === 3.U, false.B, pipeline(0).io.deq.valid && ~lock.io.deq.bits)
    arbiter.io.in(0).bits := edgeIn.AccessAck(pipeline(0).io.deq.bits)
    arbiter.io.in(0).bits.denied := true.B 
    arbiter.io.in(0).bits.corrupt := true.B 
    pipeline(0).io.deq.ready := Mux(responseId === 3.U, pipeline(1).io.enq.ready, (arbiter.io.in(0).ready && ~lock.io.deq.bits))
    
    printf(cf" deqr = ${pipeline(1).io.deq.ready}\n")
    printf(cf" enqv = ${pipeline(1).io.enq.valid}\n")
    in.d.bits := arbiter.io.out.bits
    in.d.valid := arbiter.io.out.valid 
    arbiter.io.out.ready := in.d.ready 
    
    when(edgeOut.first(out.d) && out.d.fire)
    {
        lock.io.enq.valid := true.B
        lock.io.enq.bits := true.B
        
    }.elsewhen(edgeOut.last(out.d) && out.d.fire)
    {
        printf("happening\n")
        lock.io.enq.valid := true.B
        lock.io.enq.bits := false.B
    }.otherwise
    {
        lock.io.enq.valid := false.B
    }
    //val d = Wire(Decoupled(new TLBundleD(edgeOut.bundle)))
    pipeline(1).io.enq.bits := pipeline(0).io.deq.bits
    pipeline(1).io.enq.valid := pipeline(0).io.deq.valid


    when(responseId === 3.U)
    {
        //buf(1).io.enq.bits := Cat(pipeline.io.deq.bits.data, physicalAddress)
        //buf(1).io.enq.valid := buf(0).io.deq.valid 
        //condition := true.B
        pipeline(1).io.enq.bits.address := y(65,2)
        //pipeline(1).io.enq.bits.source := 11.U
        printf(cf" Physical Address = ${physicalAddress}\n")
        printf(cf" buff 1 = ${pipeline(1).io.enq.bits}\n")
    }.elsewhen(responseId === 2.U || responseId === 1.U)
    {
        pipeline(1).io.enq.bits.address := 0.U
        //pipeline(0).io.deq.bits.denied := true.B 
        //pipeline(0).io.deq.bits.corrupt := true.B 
        //d.bits.address := buf(0).io.deq.bits
    }
    printf(cf" buff 12 = ${pipeline(1).io.deq.bits.address}\n")
    
    //val (_, pbits) = edgeOut.Put(pipeline(1).io.deq.bits.source, pipeline(1).io.deq.bits.address, 3.U, pipeline(1).io.deq.bits.data)
    //val (_, gbits) = edgeOut.Get(pipeline(1).io.deq.bits.source, pipeline(1).io.deq.bits.address, 3.U)
    //out.a.bits := Mux(pipeline(1).io.deq.bits.opcode === 0.U, pbits, gbits)
    out.a.bits := pipeline(1).io.deq.bits
    out.a.valid := pipeline(1).io.deq.valid
    printf(cf" buff 13 = ${out.a.bits.address}\n")

       /*when(valReg === true.B)
    {
        //x := memr1(addressGen(logicalAddress)) 
        y :=  combo(inpData, x)
        valReg := false.B
        printf(cf" inpdta = ${inpData}\n")
        printf(cf" x = ${x}\n")
        printf(cf" y = ${y}\n")
        
    }.elsewhen(valReg === false.B)
    {
        y := 0.U
    }*/
    //val gop = RegNext(hasdta)
    


    
    printf(cf" Master2 A Valid = ${pipeline(1).io.deq.valid}\n")
    printf(cf" Master2 Data = ${out.a.bits}\n")
    printf(cf" Master2 A Ready = ${out.a.ready}\n")
    printf(cf" Slave2 D Valid = ${out.d.valid}\n")
    printf(cf" Slave2 D Ready = ${out.d.ready}\n")
    printf(cf" Slave2 D Bits = ${out.d.bits}\n")
    //printf(cf" Master's Valid = ${valReg}\n")
    
   /* when(out.d.fire) {
            val resp = out.d.bits
            when(resp.opcode === TLMessages.AccessAckData) {
                dec := true.B
                in.d.bits := edgeIn.AccessAck(in.a.bits, out.d.bits.data)
                in.d.bits.source := 0.U
                printf(cf" Output final = ${out.d.bits}\n")
                
            }.elsewhen(resp.opcode === TLMessages.AccessAck) {
                dec := true.B
                in.d.bits := edgeIn.AccessAck(in.a.bits)
                in.d.bits.source := 0.U
                printf(cf" Output final = ${out.d.bits}\n")
            }.otherwise {
                dec := false.B
            }
        }*/
    /*when(responseId === 3.U){
        //decider1 := true.B
        val (_, pbits) = edgeOut.Put(0.U, physicalAddress, 3.U, in.a.bits.data)
        val (_, gbits) = edgeOut.Get(0.U, physicalAddress, 3.U)
        out.a.bits := Mux(gop, pbits, gbits)
        out.a.valid := decider1
        out.d.ready := true.B
    
        when(out.a.fire)
        {
            decider1 := false.B
        }
        
        printf(cf" Slave2 Data = ${out.d.bits}\n")

    }
    when(responseId === 2.U || responseId === 1.U)
    {
        in.d.bits.denied := true.B
        in.d.bits.corrupt := true.B
        //dec := true.B
        when(in.d.fire)
        {
            dec := false.B
        }
        //buf.io.enq.bits
        printf(cf" respId = ${responseId}\n")
        printf(cf" Output0 = ${in.d.bits}\n")

    }*/
    //printf(cf" respId = ${responseId}\n")
    //printf(cf" Output = ${in.d.bits}\n")
    }
}

// Master node sending address requests
class MasterGen(implicit p: Parameters) extends LazyModule {
    val node = TLClientNode(Seq(TLMasterPortParameters.v1(Seq(TLMasterParameters.v1(name = "myMaster", sourceId = IdRange(0, 8))))))
    lazy val module = new MasterGenImp(this)
    override lazy val desiredName: String = "MasterGen"
}

class MasterGenImp(outer: MasterGen) extends LazyModuleImp(outer) {
    val io = IO(new Bundle { 
    val dataOut = Output(UInt(8.W))
    val kID = Input(UInt(5.W))  //Maximum of 32 Kernels at  a time
    val LA = Input(UInt(32.W))  //32 bit Logical Address
    val rw = Input(Bool())
    val srId = Input(UInt(5.W))
    val valid = Input(Bool())
    val dataIn = Input(UInt(64.W))
    })
val decider = RegInit(true.B)

//val sum = 0
val addrss = Cat(io.kID, io.LA)
val (tl, edgeOut) = outer.node.out(0)
val (sr, pbitss) = edgeOut.Put(io.srId, addrss, 4.U, io.dataIn)
val (sr1, gbitss) = edgeOut.Get(io.srId, addrss, 4.U)
tl.a.bits := Mux(io.rw, pbitss, gbitss)
printf(cf" last byte = ${edgeOut.last(tl.a.bits, tl.a.fire)}\n")
//tl.a.valid := decider
when(io.valid)
{
    tl.a.valid := true.B
}.otherwise
{
    tl.a.valid := false.B
}
/*when(tl.a.fire)
{
    decider := false.B
}*/
//tl.a.valid := decider
tl.d.ready := true.B
val register = RegInit(0.U(12.W))
//tl.a.bits.source := 0.U
printf(cf" Master1 Data = ${tl.a.bits}\n")
printf(cf" Master1 A Valid = ${tl.a.valid}\n")
printf(cf" Master1 A Ready = ${tl.a.ready}\n")
printf(cf" s1 D Valid = ${tl.d.valid}\n")
printf(cf" s1 D Ready = ${tl.d.ready}\n")

/*when(tl.a.fire)
    {
      decider := false.B
    }
when(!io.rw)
{
    tl.a.valid := true.B
}
*/
when(tl.d.fire) {
      val resp = tl.d.bits
      printf(cf" slave1 D Bits = ${tl.d.bits}\n")
      when(resp.opcode === TLMessages.AccessAckData) {
        io.dataOut := 1.U
        
      }.elsewhen(resp.opcode === TLMessages.AccessAck) {
        io.dataOut := 2.U 
      }.otherwise {
        io.dataOut := 0.U 
      }
    }.otherwise {
      io.dataOut := 7.U 
    }
    
    printf(cf"dataOut = ${io.dataOut}\n")
    printf("----------------------------\n")
}

