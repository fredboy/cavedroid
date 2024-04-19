#include "item.h"

Item::Item() {}

QString Item::toString() {
    return "{" + name + "," + type + "," + texture + "," + QString::number(origin_x) + "," + QString::number(origin_y) + "}";
}
