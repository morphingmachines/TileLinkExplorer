// Copyright (c) 2024 Zero ASIC Corporation
// This code is licensed under Apache License 2.0 (see LICENSE for details)

#include "switchboard.hpp"

#define NBYTES 32

int main() {
    SBTX tx;
    SBRX rx;

    // initialize connections
    fprintf(stderr,"client TX initializing\n");
    tx.init("in_port.q");
    fprintf(stderr,"client RX initializing\n");
    rx.init("out_port.q");

    // form packet

    sb_packet txp;

    for (int i = 0; i < NBYTES; i++) {
        txp.data[i] = i & 0xff;
    }

    txp.destination = 0xbeefcafe;
    txp.last = true;

    // send packet
    spsc_queue* hdl = (spsc_queue*) (tx.get_shm_handle());
    fprintf(stderr,"head: %d Tail:%d\n",hdl->cached_head, hdl->cached_tail);
    tx.send_blocking(txp);
    fprintf(stderr,"TX packet: %s\n", sb_packet_to_str(txp, NBYTES).c_str());
    fprintf(stderr,"head: %d Tail:%d\n",hdl->cached_head, hdl->cached_tail);

    // receive packet

    sb_packet rxp;
    while(rx.recv(rxp) == false);
    printf("RX packet: %s\n", sb_packet_to_str(rxp, NBYTES).c_str());
    printf("head: %d Tail:%d\n",hdl->cached_head, hdl->cached_tail);


    for (int i = 0; i < NBYTES; i++) {
        assert(rxp.data[i] == (txp.data[i] + 1));
    }

    // send a packet that will end the test

    for (int i = 0; i < NBYTES; i++) {
        txp.data[i] = 0xff;
    }
    tx.send_blocking(txp);

    // declare test as having passed for regression testing purposes

    printf("PASS!\n");

    return 0;
}
