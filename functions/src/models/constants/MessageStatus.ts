/* eslint-disable */


export class MessageStatus {
    public static readonly SENT: string = "SENT";
    public static readonly WAITING_CONFIRMATION: string = "WAITING CONFIRMATION";

    public static readonly STATUS_OPTIONS_LIST: string[] = [];

    static {

        MessageStatus.STATUS_OPTIONS_LIST.push(MessageStatus.SENT);
        MessageStatus.STATUS_OPTIONS_LIST.push(MessageStatus.WAITING_CONFIRMATION);

    }

}

