/* eslint-disable */


export class BadModelFieldException extends Error {
    constructor(message:string) {
        super(message || 'bad model field');
        this.name = 'badModelFieldException';
    }
  }