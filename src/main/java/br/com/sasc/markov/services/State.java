/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.sasc.markov.services;

import lombok.Data;

/**
 *
 * @author Cássio Tatsch (cassio.tatsch@velsis.com.br)
 */
@Data
public class State {

    Integer position;
    Double percent;
    Double value;

    public State(Integer position) {
        this.position = position;
        this.percent = new Double(0);
        this.value = new Double(0);
    }
}
