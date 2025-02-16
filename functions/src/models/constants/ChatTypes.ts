/* eslint-disable */

export class ChatTypes {

    public static readonly PRIVATE_CONVERSATION:string = "private_conversation";
    public static readonly MATCH_CONVERSATION:string = "match_conversation";
    public static CHAT_TYPES_LIST: string[];

    static {

        ChatTypes.CHAT_TYPES_LIST = [];
        ChatTypes.CHAT_TYPES_LIST.push(ChatTypes.PRIVATE_CONVERSATION);
        ChatTypes.CHAT_TYPES_LIST.push(ChatTypes.MATCH_CONVERSATION);

    }

}

