﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rummy.UI
{
    public interface IGameUIServices
    {
        FanCanvas GetPlayerCardsShape(Player player);
        MeldController GetControllerFor(Meld m);
    }
}
