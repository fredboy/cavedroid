#ifndef BLOCK_H
#define BLOCK_H

#include <QString>

#define DEFAULT_BLOCK_LEFT 0
#define DEFAULT_BLOCK_TOP 0
#define DEFAULT_BLOCK_RIGHT 0
#define DEFAULT_BLOCK_BOTTOM 0
#define DEFAULT_BLOCK_HP 0
#define DEFAULT_BLOCK_FRAMES 0
#define DEFAULT_BLOCK_DROP key
#define DEFAULT_BLOCK_META ""
#define DEFAULT_BLOCK_TEXTURE key
#define DEFAULT_BLOCK_COLLISION true
#define DEFAULT_BLOCK_BACKGROUND false
#define DEFAULT_BLOCK_TRANSPARENT false
#define DEFAULT_BLOCK_BLOCK_REQUIRED false
#define DEFAULT_BLOCK_FLUID false
#define DEFAULT_BLOCK_ANIMATED false
#define DEFAULT_BLOCK_SPRITE_LEFT 0
#define DEFAULT_BLOCK_SPRITE_TOP 0
#define DEFAULT_BLOCK_SPRITE_RIGHT 0
#define DEFAULT_BLOCK_SPRITE_BOTTOM 0

class Block
{
public:
    int id, left, top, right, bottom, hp, frames, sprite_left, sprite_top, sprite_right, sprite_bottom;
    QString key, drop, meta, texture;
    bool collision, background, transparent, block_required, fluid, animated;

    Block();
};

#endif // BLOCK_H
