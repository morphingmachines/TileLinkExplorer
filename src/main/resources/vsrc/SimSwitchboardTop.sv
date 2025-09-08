
module SimSwitchboardTop(	
  input          clk,	
                 rst,	
                 out_port_ready,	
  output         out_port_valid,	
  output [255:0] out_port_data,	
  output [31:0]  out_port_dest,	
  output         out_port_last,	
                 in_port_ready,	
  input          in_port_valid,	
  input  [255:0] in_port_data,	
  input  [31:0]  in_port_dest,	
  input          in_port_last	
);


  SwitchboardTest dutWrap (
    .clock(clk),
    .reset(rst),
    // Connect input SB port signals
    .io_in_bits_data   (in_port_data),
    .io_in_bits_dest   (in_port_dest),
    .io_in_bits_last   (in_port_last),
    .io_in_valid  (in_port_valid),
    .io_in_ready  (in_port_ready),

    // Connect output SB port signals
    .io_out_bits_data  (out_port_data),
    .io_out_bits_dest  (out_port_dest),
    .io_out_bits_last  (out_port_last),
    .io_out_valid (out_port_valid),
    .io_out_ready (out_port_ready)
  );

endmodule


