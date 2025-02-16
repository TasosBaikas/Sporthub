/* eslint-disable */

export class ChatMessageType {

    public static MESSAGE: string = "message";
    public static ONLY_FOR_FIRST_MESSAGE: string = "onlyForFirstMessage";
    public static SYMBOL: string = "symbol";
    public static USER_JOINED: string = "userJoined";
    public static USER_LEFT: string = "userLeft";
    public static DELETED: string = "deleted";
    public static PHONE_ENABLED: string = "phoneEnabled";
    public static PHONE_DISABLED: string = "phoneDisabled";

    public static CHAT_MESSAGE_TYPES_LIST: string[];

    static {

        ChatMessageType.CHAT_MESSAGE_TYPES_LIST = [];
        ChatMessageType.CHAT_MESSAGE_TYPES_LIST.push(ChatMessageType.MESSAGE);
        ChatMessageType.CHAT_MESSAGE_TYPES_LIST.push(ChatMessageType.ONLY_FOR_FIRST_MESSAGE);
        ChatMessageType.CHAT_MESSAGE_TYPES_LIST.push(ChatMessageType.SYMBOL);
        ChatMessageType.CHAT_MESSAGE_TYPES_LIST.push(ChatMessageType.USER_JOINED);
        ChatMessageType.CHAT_MESSAGE_TYPES_LIST.push(ChatMessageType.USER_LEFT);
        ChatMessageType.CHAT_MESSAGE_TYPES_LIST.push(ChatMessageType.DELETED);
        ChatMessageType.CHAT_MESSAGE_TYPES_LIST.push(ChatMessageType.PHONE_ENABLED);
        ChatMessageType.CHAT_MESSAGE_TYPES_LIST.push(ChatMessageType.PHONE_DISABLED);

    }

}

