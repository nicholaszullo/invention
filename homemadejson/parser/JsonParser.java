package homemadejson.parser;

import homemadejson.support.InputDataBuffer;
import homemadejson.support.TokenBuffer;
import homemadejson.support.ParserException;

/**
 * Main parser
 * Shane Riley
 */

public class JsonParser {

//    Two token buffers, an index, and a tokenizer

    private TokenBuffer tokenBuffer;
    private TokenBuffer elementBuffer;
    private int elementIndex = 0;
    private JsonTokenizer jsonTokenizer;

//    Constructor
    public JsonParser(TokenBuffer tokenBuffer, TokenBuffer elementBuffer) {
        this.tokenBuffer = tokenBuffer;
        this.jsonTokenizer = new JsonTokenizer(this.tokenBuffer);
        this.elementBuffer = elementBuffer;
    }

//    reinit/parse
    public void parse(InputDataBuffer dataBuffer) {
        this.elementIndex = 0;
        this.jsonTokenizer.reinit(dataBuffer, this.tokenBuffer);
        parseObject(this.jsonTokenizer);
        this.elementBuffer.count = this.elementIndex;
    }


//    Object parse
    private void parseObject(JsonTokenizer tokenizer) {
        assertHasMoreTokens(tokenizer);
        tokenizer.parseToken();
        assertThisTokenType(tokenizer.tokenType(), TokenTypes.JSON_CURLY_BRACKET_LEFT);
        setElementData(tokenizer, ElementTypes.JSON_OBJECT_START);

        tokenizer.nextToken();
        tokenizer.parseToken();
        byte tokenType = tokenizer.tokenType();

        while(tokenType != TokenTypes.JSON_CURLY_BRACKET_RIGHT) {
            assertThisTokenType(tokenType, TokenTypes.JSON_STRING_TOKEN);
            setElementData(tokenizer, ElementTypes.JSON_PROPERTY_NAME);

            tokenizer.nextToken();
            tokenizer.parseToken();
            tokenType = tokenizer.tokenType();
            assertThisTokenType(tokenType, TokenTypes.JSON_COLON);

            tokenizer.nextToken();
            tokenizer.parseToken();
            tokenType = tokenizer.tokenType();

            switch(tokenType) {
                case TokenTypes.JSON_STRING_TOKEN   : { setElementData(tokenizer, ElementTypes.JSON_PROPERTY_VALUE_STRING); } break;
                case TokenTypes.JSON_NUMBER_TOKEN   : { setElementData(tokenizer, ElementTypes.JSON_PROPERTY_VALUE_NUMBER); } break;
                case TokenTypes.JSON_BOOL_TOKEN   : { setElementData(tokenizer, ElementTypes.JSON_PROPERTY_VALUE_BOOLEAN); } break;
                case TokenTypes.JSON_NULL_TOKEN   : { setElementData(tokenizer, ElementTypes.JSON_PROPERTY_VALUE_NULL); } break;
                case TokenTypes.JSON_CURLY_BRACKET_LEFT  : { parseObject(tokenizer); } break;
                case TokenTypes.JSON_SQUARE_BRACKET_LEFT : { parseArray(tokenizer); } break;
            }

            tokenizer.nextToken();
            tokenizer.parseToken();
            tokenType = tokenizer.tokenType();
            if(tokenType == TokenTypes.JSON_COMMA) {
                tokenizer.nextToken();
                tokenizer.parseToken();
                tokenType = tokenizer.tokenType();
            }
        }
        setElementData(tokenizer, ElementTypes.JSON_OBJECT_END);
    }

//    Array parse
    private void parseArray(JsonTokenizer tokenizer) {
        setElementData(tokenizer, ElementTypes.JSON_ARRAY_START);

        tokenizer.nextToken();
        tokenizer.parseToken();

        while(tokenizer.tokenType() != TokenTypes.JSON_SQUARE_BRACKET_RIGHT) {
//            Still in array
            byte tokenType = tokenizer.tokenType();

            switch(tokenType) {
                case TokenTypes.JSON_STRING_TOKEN   : { setElementData(tokenizer, ElementTypes.JSON_ARRAY_VALUE_STRING); } break;
                case TokenTypes.JSON_NUMBER_TOKEN   : { setElementData(tokenizer, ElementTypes.JSON_ARRAY_VALUE_NUMBER); } break;
                case TokenTypes.JSON_BOOL_TOKEN   : { setElementData(tokenizer, ElementTypes.JSON_ARRAY_VALUE_BOOLEAN); } break;
                case TokenTypes.JSON_NULL_TOKEN   : { setElementData(tokenizer, ElementTypes.JSON_ARRAY_VALUE_NULL); } break;
            }

            tokenizer.nextToken();
            tokenizer.parseToken();
            tokenType = tokenizer.tokenType();
            if(tokenType == TokenTypes.JSON_COMMA) {
                tokenizer.nextToken();
                tokenizer.parseToken();
                tokenType = tokenizer.tokenType();
            }
        }
    }

//    set element
    private void setElementData(JsonTokenizer tokenizer, byte elementType) {
        this.elementBuffer.position[this.elementIndex] = tokenizer.tokenPosition();
        this.elementBuffer.length[this.elementIndex] = tokenizer.tokenLength();
        this.elementBuffer.type[this.elementIndex] = elementType;
        this.elementIndex++;
    }

//    token type assertion
    private final void assertThisTokenType(byte tokenType, byte expectedTokenType) {
        if(tokenType != expectedTokenType) {
            throw new ParserException("Token type mismatch: Expected " + expectedTokenType + " but found " + tokenType);
        }
    }

//    assert more tokens
    private void assertHasMoreTokens(JsonTokenizer tokenizer) {
        if(!tokenizer.hasMoreTokens()) {
            throw new ParserException("Expected more tokens available in the tokenizer");
        }
    }

}