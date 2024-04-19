#ifndef JSONREADER_H
#define JSONREADER_H

#include "block.h"
#include "item.h"

#include <QFile>
#include <QJsonDocument>
#include <QJsonObject>

class JsonReader
{
public:
    JsonReader();
    QList<Item> parse_items(const QJsonObject &json);
    QList<Block> parse_blocks(const QJsonObject &json);
    QJsonDocument load_document(const QString &path);
    QJsonDocument serialize(const QList<Block> &blocks, const QList<Item> &items);

};

#endif // JSONREADER_H
