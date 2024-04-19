#ifndef ITEM_H
#define ITEM_H

#include <QString>

#define DEFAULT_ITEM_NAME key
#define DEFAULT_ITEM_TYPE "item"
#define DEFAULT_ITEM_TEXTURE key
#define DEFAULT_ITEM_ORIGIN_X 0.0
#define DEFAULT_ITEM_ORIGIN_Y 1.0

class Item
{
public:
    int id;
    QString key, name, type, texture;
    float origin_x, origin_y;

    Item();

    QString toString();
};

#endif // ITEM_H
