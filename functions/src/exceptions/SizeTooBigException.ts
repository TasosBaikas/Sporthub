/* eslint-disable */


export class SizeTooBigException extends Error {
    constructor(message:string) {
        super(message || 'size too big');
        this.name = 'SizeTooBigException';
    }
}
