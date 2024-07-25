package cscc43.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import cscc43.Stock.StockRepo;

@ShellComponent
public class StockCommand {
    private StockRepo stockRepo;

    public StockCommand(StockRepo stockRepo) {
        this.stockRepo = stockRepo;
    }
    
}
