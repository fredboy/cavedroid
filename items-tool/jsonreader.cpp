#include "jsonreader.h"

#include <cstdio>

JsonReader::JsonReader() {}


QList<Item> JsonReader::parse_items(const QJsonObject &json) {
    QList<Item> items;
    const QList<QString> keys = json.keys();

    int count = 0;
    for (auto &key : keys) {
        const QJsonObject json_item = json.value(key).toObject();

        const QJsonValue id = json_item["id"];
        const QJsonValue name = json_item["name"];
        const QJsonValue type = json_item["type"];
        const QJsonValue texture = json_item["texture"];
        const QJsonValue origin_x = json_item["origin_x"];
        const QJsonValue origin_y = json_item["origin_y"];

        Item item;

        item.key = key;
        item.id = id.toInt(count);
        item.name = name.toString(DEFAULT_ITEM_NAME);
        item.type = type.toString(DEFAULT_ITEM_TYPE);
        item.texture = texture.toString(DEFAULT_ITEM_TEXTURE);
        item.origin_x = (float) qBound(0.0, origin_x.toDouble(DEFAULT_ITEM_ORIGIN_X), 1.0);
        item.origin_y = (float) qBound(0.0, origin_y.toDouble(DEFAULT_ITEM_ORIGIN_Y), 1.0);

        items.append(item);

        if (item.id >= count) {
            count++;
        }
    }

    return items;
}

QList<Block> JsonReader::parse_blocks(const QJsonObject &json) {
    QList<Block> blocks;
    const QList<QString> keys = json.keys();

    int count = 0;
    for (auto &key : keys) {
        const QJsonObject json_block = json.value(key).toObject();

        const QJsonValue id = json_block["id"];
        const QJsonValue left = json_block["left"];
        const QJsonValue top = json_block["top"];
        const QJsonValue right = json_block["right"];
        const QJsonValue bottom = json_block["bottom"];
        const QJsonValue hp = json_block["hp"];
        const QJsonValue frames = json_block["frames"];
        const QJsonValue drop = json_block["drop"];
        const QJsonValue meta = json_block["meta"];
        const QJsonValue texture = json_block["texture"];
        const QJsonValue collision = json_block["collision"];
        const QJsonValue background = json_block["background"];
        const QJsonValue transparent = json_block["transparent"];
        const QJsonValue block_required = json_block["block_required"];
        const QJsonValue fluid = json_block["fluid"];
        const QJsonValue animated = json_block["animated"];
        const QJsonValue sprite_left = json_block["sprite_left"];
        const QJsonValue sprite_top = json_block["sprite_top"];
        const QJsonValue sprite_right = json_block["sprite_right"];
        const QJsonValue sprite_bottom = json_block["sprite_bottom"];

        Block block;

        block.key = key;
        block.id = id.toInt(count);
        block.left = qBound(0, left.toInt(DEFAULT_BLOCK_LEFT), 16);
        block.top = qBound(0, top.toInt(DEFAULT_BLOCK_TOP), 16);
        block.right = qBound(0, right.toInt(DEFAULT_BLOCK_RIGHT), 16 - block.left);
        block.bottom = qBound(0, bottom.toInt(DEFAULT_BLOCK_BOTTOM), 16 - block.top);
        block.hp = hp.toInt(DEFAULT_BLOCK_HP);
        block.frames = frames.toInt(DEFAULT_BLOCK_FRAMES);
        block.drop = drop.toString(DEFAULT_BLOCK_DROP);
        block.meta = meta.toString(DEFAULT_BLOCK_META);
        block.texture = texture.toString(DEFAULT_BLOCK_TEXTURE);
        block.collision = collision.toBool(DEFAULT_BLOCK_COLLISION);
        block.background = background.toBool(DEFAULT_BLOCK_BACKGROUND);
        block.transparent = transparent.toBool(DEFAULT_BLOCK_TRANSPARENT);
        block.block_required = block_required.toBool(DEFAULT_BLOCK_BLOCK_REQUIRED);
        block.fluid = fluid.toBool(DEFAULT_BLOCK_FLUID);
        block.animated = animated.toBool(DEFAULT_BLOCK_ANIMATED);
        block.sprite_left = qBound(0, sprite_left.toInt(DEFAULT_BLOCK_SPRITE_LEFT), 16);
        block.sprite_top = qBound(0, sprite_top.toInt(DEFAULT_BLOCK_SPRITE_TOP), 16);
        block.sprite_right = qBound(0, sprite_right.toInt(DEFAULT_BLOCK_SPRITE_RIGHT), 16 - block.sprite_left);
        block.sprite_bottom = qBound(0, sprite_bottom.toInt(DEFAULT_BLOCK_SPRITE_BOTTOM), 16 - block.sprite_top);

        blocks.append(block);

        if (block.id >= count) {
            count++;
        }
    }

    return blocks;
}

QJsonDocument JsonReader::load_document(const QString &path) {
    QFile fin(path);
    fin.open(QIODevice::ReadOnly);
    QByteArray ba = fin.readAll();
    fin.close();


    QJsonParseError *error = nullptr;
    QJsonDocument json = QJsonDocument::fromJson(ba, error);

    if (error) {
        std::fprintf(stderr, "Error: %s", error->errorString().toStdString().data());
        throw;
    }

    return json;
}


#define SERIALIZE_FIELD_NO_DEFAULT(json,obj,field,default) if (obj.field != default) {\
    json[#field] = obj.field; \
}

#define SERIALIZE_FIELD(json,obj,field,default) json[#field] = obj.field;



QJsonDocument JsonReader::serialize(const QList<Block> &blocks, const QList<Item> &items) {
    QJsonObject json_blocks;
    for (auto &block : blocks) {
        QJsonObject json_block;
        QString key = block.key;

        SERIALIZE_FIELD(json_block, block, id, -1);
        SERIALIZE_FIELD(json_block, block, left, DEFAULT_BLOCK_LEFT);
        SERIALIZE_FIELD(json_block, block, top, DEFAULT_BLOCK_TOP);
        SERIALIZE_FIELD(json_block, block, right, DEFAULT_BLOCK_RIGHT);
        SERIALIZE_FIELD(json_block, block, bottom, DEFAULT_BLOCK_BOTTOM);
        SERIALIZE_FIELD(json_block, block, hp, DEFAULT_BLOCK_HP);
        SERIALIZE_FIELD(json_block, block, frames, DEFAULT_BLOCK_FRAMES);
        SERIALIZE_FIELD(json_block, block, drop, DEFAULT_BLOCK_DROP);
        SERIALIZE_FIELD(json_block, block, meta, DEFAULT_BLOCK_META);
        SERIALIZE_FIELD(json_block, block, texture, DEFAULT_BLOCK_TEXTURE);
        SERIALIZE_FIELD(json_block, block, collision, DEFAULT_BLOCK_COLLISION);
        SERIALIZE_FIELD(json_block, block, background, DEFAULT_BLOCK_BACKGROUND);
        SERIALIZE_FIELD(json_block, block, transparent, DEFAULT_BLOCK_TRANSPARENT);
        SERIALIZE_FIELD(json_block, block, block_required, DEFAULT_BLOCK_BLOCK_REQUIRED);
        SERIALIZE_FIELD(json_block, block, fluid, DEFAULT_BLOCK_FLUID);
        SERIALIZE_FIELD(json_block, block, animated, DEFAULT_BLOCK_ANIMATED);
        SERIALIZE_FIELD(json_block, block, sprite_left, DEFAULT_BLOCK_SPRITE_LEFT);
        SERIALIZE_FIELD(json_block, block, sprite_top, DEFAULT_BLOCK_SPRITE_TOP);
        SERIALIZE_FIELD(json_block, block, sprite_right, DEFAULT_BLOCK_SPRITE_RIGHT);
        SERIALIZE_FIELD(json_block, block, sprite_bottom, DEFAULT_BLOCK_SPRITE_BOTTOM);

        json_blocks[key] = json_block;
    }

    QJsonObject json_items;
    for (auto &item : items) {
        QJsonObject json_item;
        QString key = item.key;

        SERIALIZE_FIELD(json_item, item, id, -1);
        SERIALIZE_FIELD(json_item, item, name, DEFAULT_ITEM_NAME);
        SERIALIZE_FIELD(json_item, item, type, DEFAULT_ITEM_TYPE);
        SERIALIZE_FIELD(json_item, item, texture, DEFAULT_ITEM_TEXTURE);
        SERIALIZE_FIELD(json_item, item, origin_x, DEFAULT_ITEM_ORIGIN_X);
        SERIALIZE_FIELD(json_item, item, origin_y, DEFAULT_ITEM_ORIGIN_Y);

        json_items[key] = json_item;
    }

    QJsonObject root;
    root["blocks"] = json_blocks;
    root["items"] = json_items;

    return QJsonDocument(root);
}
