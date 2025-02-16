/* eslint-disable */


export class ElementNotFoundException extends Error {
    constructor(message:string) {
        super(message || 'Item not found');
        this.name = 'ItemNotFoundError';
    }
  }