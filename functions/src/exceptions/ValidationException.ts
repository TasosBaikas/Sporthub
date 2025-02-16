/* eslint-disable */


export class ValidationException extends Error {
    constructor(message:string) {
        super(message || 'validation failed');
        this.name = 'ValidationException';
    }
}
