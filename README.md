TileLink Explorer
=======================

A toy project to explore TileLink with the diplomacy framework. This project uses [playground](https://github.com/morphingmachines/playground.git) as a library. `playground` and `TileLinkExplorer` directories should be at the same level, as shown below.  
```
  workspace
  |-- playground
  |-- TileLinkExplorer
```
Make sure that you have a working [playground](https://github.com/morphingmachines/playground.git) project before proceeding further. Do not rename/modify `playground` directory structure.

## Clone the repository
Clone this repository into the same directory that contains `playground`.
```bash
$ git clone https://github.com/morphingmachines/TileLinkExplorer.git
```
### Generating RTL
```bash
$ make rtl
```
The output verilog files are generated in the ./generated_sv_dir directory. This also generates a graphml file that visualizes the diplomacy graph of different components in the system. To view graphml file, use yEd.

## Scala console
```bash
$ make console
```

You load a design into the console for interacting running, as shown below

```bash
scala> :load inConsole.scala
```

`inConsole.scala` will load `Poin2Point` module in the console. We can query module parameter once it is loaded, as shown below.

```
scala> dut.manager1.node.in.length
val res1: Int = 1
scala> dut.manager1.node.out.length
val res2: Int = 0
```

## Chisel Learning Resources

- [Chisel Book](https://github.com/schoeberl/chisel-book)
- [Chisel Documentation](https://www.chisel-lang.org/chisel3/)
- [Chisel API](https://www.chisel-lang.org/api/latest/)




