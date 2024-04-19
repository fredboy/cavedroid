#include "mainwindow.h"
#include "./ui_mainwindow.h"
#include "jsonreader.h"
#include "thirdparty/qjsonmodel.h"

#include <QFileDialog>
#include <QStandardItemModel>

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindow)
{
    ui->setupUi(this);
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::on_actionOpen_triggered()
{
    QString input_file = QFileDialog::getOpenFileName(this, "Choose items json", ".", "Json file (*.json)");

    JsonReader reader;
    QJsonDocument json_document = reader.load_document(input_file);

    QJsonValue json_blocks = json_document["blocks"];
    QJsonValue json_items = json_document["items"];

    QList<Block> blocks = reader.parse_blocks(json_blocks.toObject());
    QList<Item> items = reader.parse_items(json_items.toObject());

    QJsonDocument serialized = reader.serialize(blocks, items);

    QByteArray ba = serialized.toJson();

    QJsonModel *model = new QJsonModel();

    ui->treeView->setModel(model);
    ui->tableView->setModel(model);

    model->loadJson(ba);
}

