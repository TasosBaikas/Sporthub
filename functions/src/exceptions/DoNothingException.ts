/* eslint-disable */


export class DoNothingException extends Error {
    constructor(message:string) {
        super(message || 'do nothing');
        this.name = 'DoNothingException';
    }
}
