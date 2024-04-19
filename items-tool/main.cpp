#include "mainwindow.h"
#include "item.h"
#include "block.h"

#include <QApplication>
#include <QCommandLineParser>
#include <QFile>
#include <QJsonDocument>
#include <QJsonObject>
#include <iostream>






int main(int argc, char *argv[])
{
    QApplication app(argc, argv);
    MainWindow w;
    QCommandLineParser parser;

 //    QCoreApplication::setApplicationVersion("alpha0.5.1");

 //    parser.addVersionOption();
 //    parser.addHelpOption();
 //    parser.addPositionalArgument("input", "input json");
 //    parser.addPositionalArgument("output", "output json");

 //    parser.process(app);

 //    QList<QString> args = parser.positionalArguments();

 //    if (args.length() < 2) {
 //        return 1;
 //    }

 //    QString inputPath = args[0];
 //    QString outputPath = args[1];



 //    QJsonObject obj = json.object();

 //    QList<Item> items = parse_items(obj["items"].toObject());
 //    QList<Block> blocks = parse_blocks(obj["blocks"].toObject());

 //    QJsonDocument result = serialize(blocks, items);

 //    QFile fout(outputPath);
 //    fout.open(QIODevice::WriteOnly);
 //    QByteArray b = result.toJson();
 //    fout.write(b);
 //    fout.close();

 //    return 0;

    w.show();

    return app.exec();
}
